import { useEffect, useState } from 'react';
import api from '../api/axios';
import Layout from '../components/Layout';

const ALL_ROLES = [
  'EMPLOYEE',
  'TEAM_LEAD_MANAGER',
  'HR_SPECIALIST',
  'DEPARTMENT_HEAD',
  'LEARNING_DEVELOPMENT_ADMIN',
  'SYSTEM_ADMINISTRATOR',
];

export default function AdminConsole() {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [editingId, setEditingId] = useState(null);
  const [selectedRoles, setSelectedRoles] = useState([]);

  const loadUsers = async () => {
    setLoading(true);
    try {
      const { data } = await api.get('/admin/users');
      setUsers(data.data);
    } catch (err) {
      setError('Failed to load users. Are you logged in as a System Administrator?');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadUsers();
  }, []);

  const startEditRoles = (u) => {
    setEditingId(u.id);
    setSelectedRoles(u.roles);
  };

  const toggleRole = (role) => {
    setSelectedRoles((prev) =>
      prev.includes(role) ? prev.filter((r) => r !== role) : [...prev, role]
    );
  };

  const saveRoles = async (id) => {
    try {
      await api.put(`/admin/users/${id}/roles`, { roles: selectedRoles });
      setEditingId(null);
      loadUsers();
    } catch (err) {
      alert('Failed to update roles: ' + (err.response?.data?.message || 'Unknown error'));
    }
  };

  const toggleEnabled = async (u) => {
    try {
      await api.patch(`/admin/users/${u.id}/toggle`, { enabled: !u.enabled });
      loadUsers();
    } catch (err) {
      alert('Failed to toggle user status');
    }
  };

  const deleteUser = async (id) => {
    if (!confirm('Delete this user permanently?')) return;
    try {
      await api.delete(`/admin/users/${id}`);
      loadUsers();
    } catch (err) {
      alert('Failed to delete user');
    }
  };

  return (
    <Layout title="Admin Console" subtitle="Manage platform users and role assignments">
      {error && <div className="alert alert-error">{error}</div>}

        {loading ? (
          <div className="loading-text">Loading users...</div>
        ) : (
          <table>
            <thead>
              <tr>
                <th>Name</th>
                <th>Email</th>
                <th>Department</th>
                <th>Roles</th>
                <th>Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {users.map((u) => (
                <tr key={u.id}>
                  <td>{u.fullName}</td>
                  <td>{u.email}</td>
                  <td>{u.department || '—'}</td>
                  <td>
                    {editingId === u.id ? (
                      <div style={{ display: 'flex', flexWrap: 'wrap', gap: 6, maxWidth: 260 }}>
                        {ALL_ROLES.map((role) => (
                          <label key={role} style={{ fontSize: 12, display: 'flex', alignItems: 'center', gap: 4 }}>
                            <input
                              type="checkbox"
                              checked={selectedRoles.includes(role)}
                              onChange={() => toggleRole(role)}
                            />
                            {role.replaceAll('_', ' ')}
                          </label>
                        ))}
                      </div>
                    ) : (
                      u.roles.map((r) => <span key={r} className="role-badge">{r.replaceAll('_', ' ')}</span>)
                    )}
                  </td>
                  <td>
                    <span className={`status-pill ${u.enabled ? 'status-active' : 'status-disabled'}`}>
                      {u.enabled ? 'Active' : 'Disabled'}
                    </span>
                  </td>
                  <td>
                    {editingId === u.id ? (
                      <>
                        <button className="btn-sm" onClick={() => saveRoles(u.id)}>Save</button>
                        <button className="btn-sm" onClick={() => setEditingId(null)}>Cancel</button>
                      </>
                    ) : (
                      <>
                        <button className="btn-sm" onClick={() => startEditRoles(u)}>Edit Roles</button>
                        <button className="btn-sm" onClick={() => toggleEnabled(u)}>
                          {u.enabled ? 'Disable' : 'Enable'}
                        </button>
                        <button className="btn-sm" onClick={() => deleteUser(u.id)} style={{ color: '#dc2626' }}>
                          Delete
                        </button>
                      </>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
    </Layout>
  );
}
