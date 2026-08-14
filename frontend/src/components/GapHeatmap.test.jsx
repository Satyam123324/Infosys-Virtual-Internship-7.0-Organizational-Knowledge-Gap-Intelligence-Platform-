import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import GapHeatmap from './GapHeatmap';

describe('GapHeatmap', () => {
  it('shows an empty state when there is no benchmarked data', () => {
    render(<GapHeatmap reports={[]} />);
    expect(screen.getByText(/no benchmarked gap data/i)).toBeInTheDocument();
  });

  it('renders departments and skills from the reports', () => {
    const reports = [
      {
        frameworkFound: true,
        departmentName: 'Engineering',
        gaps: [{ skillName: 'Java', gapSize: 3, severity: 'CRITICAL' }],
      },
      {
        frameworkFound: true,
        departmentName: 'Design',
        gaps: [{ skillName: 'Teamwork', gapSize: 1, severity: 'MINOR' }],
      },
    ];
    render(<GapHeatmap reports={reports} />);

    expect(screen.getByText('Engineering')).toBeInTheDocument();
    expect(screen.getByText('Design')).toBeInTheDocument();
    expect(screen.getByText('Java')).toBeInTheDocument();
    expect(screen.getByText('Teamwork')).toBeInTheDocument();
  });
});
