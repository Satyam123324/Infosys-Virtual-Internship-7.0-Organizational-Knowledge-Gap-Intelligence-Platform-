export default function AuthRadarChart() {
  // Static illustrative data: Required (outer diamond) vs Current (inner, shows the gap)
  // Axes: top=Java, right=Communication, bottom=Cloud, left=Leadership
  const cx = 100, cy = 100;
  const rings = [20, 40, 60, 80];

  const requiredPoints = '100,20 180,100 100,180 20,100';
  const currentPoints = '100,56 168,100 100,128 48,100';

  return (
    <svg width="220" height="220" viewBox="0 0 200 200" fill="none">
      {/* grid rings */}
      {rings.map((r) => (
        <polygon
          key={r}
          points={`100,${100 - r} ${100 + r},100 100,${100 + r} ${100 - r},100`}
          stroke="rgba(148,163,184,0.18)"
          strokeWidth="1"
          fill="none"
        />
      ))}
      {/* axis lines */}
      <line x1="100" y1="20" x2="100" y2="180" stroke="rgba(148,163,184,0.18)" strokeWidth="1" />
      <line x1="20" y1="100" x2="180" y2="100" stroke="rgba(148,163,184,0.18)" strokeWidth="1" />

      {/* required (target) */}
      <polygon points={requiredPoints} stroke="#f59e0b" strokeWidth="1.5" strokeDasharray="4 3" fill="rgba(245,158,11,0.06)" />

      {/* current (actual) */}
      <polygon points={currentPoints} stroke="#14b8a6" strokeWidth="2" fill="rgba(20,184,166,0.22)" />

      {/* live-detection pulse on the biggest gap (Cloud, bottom) */}
      <circle cx="100" cy="128" r="4" fill="#14b8a6" />
      <circle cx="100" cy="128" r="4" fill="#14b8a6">
        <animate attributeName="r" from="4" to="14" dur="1.8s" repeatCount="indefinite" />
        <animate attributeName="opacity" from="0.6" to="0" dur="1.8s" repeatCount="indefinite" />
      </circle>

      {/* axis labels */}
      <text x="100" y="12" textAnchor="middle" fontSize="10" fill="#cbd5e1" fontFamily="Inter, sans-serif">Java</text>
      <text x="192" y="103" textAnchor="start" fontSize="10" fill="#cbd5e1" fontFamily="Inter, sans-serif">Communication</text>
      <text x="100" y="196" textAnchor="middle" fontSize="10" fill="#cbd5e1" fontFamily="Inter, sans-serif">Cloud</text>
      <text x="8" y="103" textAnchor="end" fontSize="10" fill="#cbd5e1" fontFamily="Inter, sans-serif">Leadership</text>
    </svg>
  );
}
