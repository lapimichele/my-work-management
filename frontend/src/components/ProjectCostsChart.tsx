import React, { useEffect, useState, useMemo } from 'react';
import * as Recharts from 'recharts';
import { TooltipProps } from 'recharts';
import { ProjectCost, statisticsService } from '../services/statisticsService';

const { LabelList } = Recharts;

// Custom Tooltip for Recharts
const CustomTooltip: React.FC<TooltipProps<number, string>> = ({ active, payload, label }) => {
  if (!active || !payload || !payload.length) return null;
  return (
    <div style={{ background: '#0a0a0a', border: '1px solid #eee', padding: 10, borderRadius: 4 }}>
      <div style={{ fontWeight: 'bold', marginBottom: 4 }}>Period: {label}</div>
      {payload.map((entry, idx) => (
        <div key={idx} style={{ color: (entry && entry.color) || undefined }}>
          {entry.name}: {entry.value && typeof entry.value === 'number' ? entry.value.toLocaleString() : entry.value}
        </div>
      ))}
    </div>
  );
};

const ProjectCostsChart: React.FC = () => {
  const [rawCosts, setRawCosts] = useState<ProjectCost[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [groupBy, setGroupBy] = useState<'month' | 'year'>('month');

  useEffect(() => {
    const fetchData = async () => {
      try {
        const costs = await statisticsService.getProjectCosts();
        setRawCosts(costs);
      } catch (err) {
        setError('Failed to fetch project costs');
        console.error('Error fetching project costs:', err);
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, []);

  const processedData = useMemo(() => {
    const dataMap = new Map<string, { [key: string]: string | number }>();
    const projectNames = new Set<string>();

    rawCosts.forEach(item => {
      // Normalize to 'YYYY-MM' or 'YYYY' depending on groupBy
      let groupKey = item.month;
      // Try to parse and normalize if needed
      if (/^\d{2}\/\d{4}$/.test(item.month)) { // e.g. '06/2024'
        const [mm, yyyy] = item.month.split('/');
        groupKey = `${yyyy}-${mm}`;
      }
      if (groupBy === 'year') {
        // Extract year from 'YYYY-MM' or 'MM/YYYY'
        if (/^\d{4}-\d{2}$/.test(groupKey)) {
          groupKey = groupKey.slice(0, 4);
        } else if (/^\d{2}\/\d{4}$/.test(item.month)) {
          groupKey = item.month.split('/')[1];
        }
      }
      if (!dataMap.has(groupKey)) {
        dataMap.set(groupKey, { group: groupKey });
      }
      const groupData = dataMap.get(groupKey)!;
      groupData[item.projectName] = (groupData[item.projectName] || 0) as number + item.totalCost;
      projectNames.add(item.projectName);
    });

    // Convert Map values to array and sort by group (chronologically)
    const sortedData = Array.from(dataMap.values()).sort((a, b) => {
      const gA = a.group as string;
      const gB = b.group as string;
      return gA.localeCompare(gB);
    });
    return { data: sortedData, projectNames: Array.from(projectNames).sort() };
  }, [rawCosts, groupBy]);

  // Simple color palette (you might want a more sophisticated one)
  const colors = useMemo(() => {
    const projectColors: { [key: string]: string } = {};
    const baseColors = [
      '#8884d8', '#82ca9d', '#ffc658', '#ff7300', '#a4de6c', '#d0ed57', '#83a6ed', '#8dd1e1'
    ];
    processedData.projectNames.forEach((name, index) => {
      projectColors[name] = baseColors[index % baseColors.length];
    });
    return projectColors;
  }, [processedData.projectNames]);

  if (loading) {
    return <div className="animate-pulse">Loading project costs...</div>;
  }

  if (error) {
    return <div className="text-red-500">{error}</div>;
  }

  return (
    <div className="bg-gray-100 dark:bg-gray-800 p-6 rounded-lg shadow-md">
      <div className="flex justify-between items-center mb-4">
        <h3 className="text-lg font-semibold text-gray-900 dark:text-white">
          Project Costs by {groupBy === 'month' ? 'Month' : 'Year'}
        </h3>
        <div>
          <label htmlFor="groupBy" className="mr-2 font-medium text-gray-700 dark:text-gray-200">Group by:</label>
          <select
            id="groupBy"
            value={groupBy}
            onChange={e => setGroupBy(e.target.value as 'month' | 'year')}
            className="p-2 border border-gray-300 rounded-md dark:bg-gray-700 dark:border-gray-600 dark:text-white"
          >
            <option value="month">Month</option>
            <option value="year">Year</option>
          </select>
        </div>
      </div>
      <div className="h-[400px]">
        <Recharts.ResponsiveContainer width="100%" height="100%">
          <Recharts.BarChart
            data={processedData.data}
            margin={{
              top: 20,
              right: 30,
              left: 20,
              bottom: 5,
            }}
          >
            <Recharts.CartesianGrid strokeDasharray="3 3" />
            <Recharts.XAxis dataKey="group" />
            <Recharts.YAxis />
            <Recharts.Tooltip content={<CustomTooltip />} />
            <Recharts.Legend />
            {processedData.projectNames.map(name => (
              <Recharts.Bar key={name} dataKey={name} stackId="a" fill={colors[name]} name={name}>
                {groupBy === 'year' && (
                  <LabelList dataKey={name} 
                    position="top" 
                    formatter={(value: number) => value?.toLocaleString?.() ?? value} 
                    style={{ fontSize: 12, fill: 'white' }} />
                )}
                {groupBy === 'month' && (
                  <LabelList
                    dataKey={name}
                    position="top"
                    angle={-90}
                    offset={20}
                    formatter={(value: number) => value?.toLocaleString?.() ?? value}
                    style={{ textAnchor: 'middle', fontSize: 12, fill: 'white' }}
                  />
                )}
              </Recharts.Bar>
            ))}
          </Recharts.BarChart>
        </Recharts.ResponsiveContainer>
      </div>
    </div>
  );
};

export default ProjectCostsChart; 