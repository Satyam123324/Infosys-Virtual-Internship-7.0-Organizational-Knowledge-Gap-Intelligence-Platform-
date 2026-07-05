import { useEffect, useState } from 'react';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, PieChart, Pie, Cell, Legend } from 'recharts';
import { employeeApi } from '../../api/employeeApi';
import { adminApi } from '../../api/adminApi';

const COLORS = ['#4f46e5', '#7c3aed', '#2563eb', '#0891b2', '#16a34a', '#ca8a04', '#dc2626'];

export default function AdminHRView() {
  const [profiles, setProfiles] = useState([]);
  const [departments, setDepartments] = useState([]);
  const [skills, setSkills] = useState([]);
  const [totalUsers, setTotalUsers] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    (async () => {
      try {
        const [profilesRes, deptRes, skillsRes, usersRes] = await Promise.all([
          employeeApi.getAllProfiles(),
          employeeApi.getAllDepartments(),
          employeeApi.getAllSkills(),
          adminApi.getAllUsers().catch(() => ({ data: { data: [] } })),
        ]);
        setProfiles(profilesRes.data.data);
        setDepartments(deptRes.data.data);
        setSkills(skillsRes.data.data);
        setTotalUsers(usersRes.data.data.length);
      } catch (err) {
        setError('Failed to load organization-wide analytics. HR Specialist or Admin access required.');
      } finally {
        setLoading(false);
      }
    })();
  }, []);

  // Headcount per department
  const deptHeadcount = departments.map((d) => ({
    name: d.name,
    employees: profiles.filter((p) => p.departmentId === d.id).length,
  }));

  // Skill category distribution
  const categoryCounts = {};
  skills.forEach((s) => {
    const cat = s.categoryName || 'Uncategorized';
    categoryCounts[cat] = (categoryCounts[cat] || 0) + 1;
  });
  const categoryData = Object.entries(categoryCounts).map(([name, value]) => ({ name, value }));

  const totalSkillsLogged = profiles.reduce((sum, p) => sum + (p.skills?.length || 0), 0);

  if (error) {
    return <div className="alert alert-error">{error}</div>;
  }

  return (
    <>
      <div className="card-grid">
        <div className="info-card">
          <div className="label">Total Users</div>
          <div className="value">{loading ? '—' : totalUsers}</div>
        </div>
        <div className="info-card">
          <div className="label">Departments</div>
          <div className="value">{loading ? '—' : departments.length}</div>
        </div>
        <div className="info-card">
          <div className="label">Skills in Catalog</div>
          <div className="value">{loading ? '—' : skills.length}</div>
        </div>
        <div className="info-card">
          <div className="label">Skills Logged (org-wide)</div>
          <div className="value">{loading ? '—' : totalSkillsLogged}</div>
        </div>
      </div>

      {loading ? (
        <div className="loading-text">Loading analytics...</div>
      ) : (
        <div style={{ display: 'grid', gridTemplateColumns: '1.3fr 1fr', gap: 20, marginTop: 8 }}>
          <div className="info-card">
            <div className="section-title" style={{ marginTop: 0 }}>Headcount by Department</div>
            <ResponsiveContainer width="100%" height={260}>
              <BarChart data={deptHeadcount}>
                <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
                <XAxis dataKey="name" tick={{ fontSize: 12 }} />
                <YAxis allowDecimals={false} tick={{ fontSize: 12 }} />
                <Tooltip />
                <Bar dataKey="employees" fill="#4f46e5" radius={[6, 6, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </div>

          <div className="info-card">
            <div className="section-title" style={{ marginTop: 0 }}>Skill Catalog by Category</div>
            <ResponsiveContainer width="100%" height={260}>
              <PieChart>
                <Pie data={categoryData} dataKey="value" nameKey="name" cx="50%" cy="50%" outerRadius={80} label>
                  {categoryData.map((entry, index) => (
                    <Cell key={entry.name} fill={COLORS[index % COLORS.length]} />
                  ))}
                </Pie>
                <Tooltip />
                <Legend wrapperStyle={{ fontSize: 12 }} />
              </PieChart>
            </ResponsiveContainer>
          </div>
        </div>
      )}

      <div className="section-title">All Employee Profiles</div>
      <table>
        <thead>
          <tr>
            <th>Name</th>
            <th>Department</th>
            <th>Role Title</th>
            <th>Skills</th>
            <th>Certifications</th>
          </tr>
        </thead>
        <tbody>
          {profiles.map((p) => (
            <tr key={p.id}>
              <td>{p.fullName}</td>
              <td>{p.departmentName || '—'}</td>
              <td>{p.currentRoleTitle || '—'}</td>
              <td>{p.skills?.length || 0}</td>
              <td>{p.certifications?.length || 0}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </>
  );
}
