import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
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
      <div className="card-grid">
        <div className="info-card">
          <div className="label">Full Name</div>
          <div className="value">{user.fullName}</div>
        </div>
        <div className="info-card">
          <div className="label">Email</div>
          <div className="value">{user.email}</div>
        </div>
        <div className="info-card">
          <div className="label">Skills Tracked</div>
          <div className="value">{loading ? '—' : skills.length}</div>
        </div>
        <div className="info-card">
          <div className="label">Certifications</div>
          <div className="value">{loading ? '—' : certifications.length}</div>
        </div>
      </div>

      <div className="section-title">My Recommended Next Step</div>
      <div className="info-card" style={{ color: '#374151', fontSize: 14 }}>
        Keep your skill inventory up to date so managers and HR can identify the right
        training for you. <Link to="/skills" style={{ color: '#4f46e5', fontWeight: 600 }}>Update my skills →</Link>
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
