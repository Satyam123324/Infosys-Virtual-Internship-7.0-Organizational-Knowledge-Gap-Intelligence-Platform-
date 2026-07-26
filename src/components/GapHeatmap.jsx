import { useMemo } from 'react';

// Colour a cell by the average skill-gap size (0 = meeting the bar, 4 = unaware
// vs expert). Green → red as the gap widens.
function cellStyle(avg) {
  if (avg === null || avg === undefined) return { background: '#f8fafc', color: '#cbd5e1' };
  if (avg < 0.5) return { background: '#dcfce7', color: '#15803d' };
  if (avg < 1.5) return { background: '#fef9c3', color: '#a16207' };
  if (avg < 2.5) return { background: '#fed7aa', color: '#c2410c' };
  return { background: '#fecaca', color: '#b91c1c' };
}

function LegendItem({ color, label }) {
  return (
    <span style={{ display: 'inline-flex', alignItems: 'center', gap: 6 }}>
      <span style={{ width: 14, height: 14, borderRadius: 3, background: color, border: '1px solid #e2e8f0' }} />
      {label}
    </span>
  );
}

/**
 * Org-wide gap heatmap: rows = departments, columns = skills, each cell shaded
 * by the average gap size for that department/skill. Built entirely from the
 * live gap-analysis reports, no extra backend call.
 */
export default function GapHeatmap({ reports }) {
  const { departments, skills, matrix } = useMemo(() => {
    const withFramework = (reports || []).filter((r) => r.frameworkFound && Array.isArray(r.gaps));

    const deptSet = new Set();
    const skillSet = new Set();
    const acc = {}; // acc[dept][skill] = { sum, count }

    withFramework.forEach((r) => {
      const dept = r.departmentName || 'Unassigned';
      deptSet.add(dept);
      (r.gaps || []).forEach((g) => {
        skillSet.add(g.skillName);
        acc[dept] = acc[dept] || {};
        acc[dept][g.skillName] = acc[dept][g.skillName] || { sum: 0, count: 0 };
        acc[dept][g.skillName].sum += g.gapSize;
        acc[dept][g.skillName].count += 1;
      });
    });

    const departments = [...deptSet].sort();
    const skills = [...skillSet].sort();
    const matrix = departments.map((d) =>
      skills.map((s) => {
        const cell = acc[d] && acc[d][s];
        return cell && cell.count ? cell.sum / cell.count : null;
      })
    );
    return { departments, skills, matrix };
  }, [reports]);

  if (skills.length === 0) {
    return <div className="info-card" style={{ color: '#64748b' }}>No benchmarked gap data to chart yet.</div>;
  }

  return (
    <div>
      <div style={{ overflowX: 'auto', border: '1px solid #e2e8f0', borderRadius: 10 }}>
        <table style={{ borderCollapse: 'collapse', width: '100%', fontSize: 12 }}>
          <thead>
            <tr>
              <th style={{ textAlign: 'left', padding: '8px 12px', position: 'sticky', left: 0, background: '#f8fafc', color: '#475569', zIndex: 1 }}>
                Department
              </th>
              {skills.map((s) => (
                <th key={s} style={{ padding: '6px 4px', color: '#475569', fontWeight: 600, height: 96, verticalAlign: 'bottom' }}>
                  <div style={{ writingMode: 'vertical-rl', transform: 'rotate(180deg)', margin: '0 auto', whiteSpace: 'nowrap' }}>{s}</div>
                </th>
              ))}
            </tr>
          </thead>
          <tbody>
            {departments.map((d, i) => (
              <tr key={d}>
                <td style={{ padding: '8px 12px', fontWeight: 600, color: '#0f172a', position: 'sticky', left: 0, background: 'white', whiteSpace: 'nowrap', zIndex: 1 }}>
                  {d}
                </td>
                {skills.map((s, j) => {
                  const avg = matrix[i][j];
                  const st = cellStyle(avg);
                  return (
                    <td
                      key={s}
                      title={avg === null ? `${d} · ${s}: not required` : `${d} · ${s}: average gap ${avg.toFixed(1)}`}
                      style={{
                        textAlign: 'center', padding: '10px 8px', minWidth: 46,
                        background: st.background, color: st.color, fontWeight: 600,
                        borderRight: '2px solid #fff', borderBottom: '2px solid #fff',
                      }}
                    >
                      {avg === null ? '' : avg.toFixed(1)}
                    </td>
                  );
                })}
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      <div style={{ display: 'flex', gap: 16, marginTop: 12, fontSize: 12, color: '#64748b', flexWrap: 'wrap' }}>
        <LegendItem color="#dcfce7" label="On track (< 0.5)" />
        <LegendItem color="#fef9c3" label="Minor (0.5–1.5)" />
        <LegendItem color="#fed7aa" label="Moderate (1.5–2.5)" />
        <LegendItem color="#fecaca" label="Critical (> 2.5)" />
        <LegendItem color="#f8fafc" label="Not required" />
      </div>
    </div>
  );
}
