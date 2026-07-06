import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { ListChecks, Award, ArrowRight } from 'lucide-react';
import { employeeApi } from '../../api/employeeApi';

export default function EmployeeView({ user }) {
  const [skills, setSkills] = useState([]);
  const [certifications, setCertifications] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    (async () => {
      try {
        const [skillsRes, certsRes] = await Promise.all([
          employeeApi.getMySkills(),
          employeeApi.getMyCertifications(),
        ]);
        setSkills(skillsRes.data.data);
        setCertifications(certsRes.data.data);
      } catch (err) {
        // fail quietly, dashboard still shows profile info
      } finally {
        setLoading(false);
      }
    })();
  }, []);

  return (
    <>
      <div className="hero-card">
        <div className="hero-eyebrow">My Workspace</div>
        <div className="hero-title">{user.fullName}</div>
        <div className="hero-sub">{user.email} · {user.department || 'No department set'}</div>
      </div>

      <div className="card-grid">
        <div className="stat-card">
          <div className="stat-icon teal"><ListChecks size={17} /></div>
          <div className="stat-label">Skills Tracked</div>
          <div className="stat-value">{loading ? '—' : skills.length}</div>
        </div>
        <div className="stat-card">
          <div className="stat-icon amber"><Award size={17} /></div>
          <div className="stat-label">Certifications</div>
          <div className="stat-value">{loading ? '—' : certifications.length}</div>
        </div>
      </div>

      <div className="section-title">My Recommended Next Step</div>
      <div className="info-card" style={{ color: '#334155', fontSize: 14, display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
        <span>Keep your skill inventory up to date so managers and HR can identify the right training for you.</span>
        <Link to="/skills" style={{ color: '#0d9488', fontWeight: 600, display: 'flex', alignItems: 'center', gap: 4, whiteSpace: 'nowrap', marginLeft: 16 }}>
          Update my skills <ArrowRight size={15} />
        </Link>
      </div>

      <div className="section-title">Assigned Roles</div>
      <div>
        {user.roles?.map((role) => (
          <span key={role} className="role-badge">{role.replaceAll('_', ' ')}</span>
        ))}
      </div>
    </>
  );
}
