import api from './axios';

export const adminApi = {
  getAllUsers: () => api.get('/admin/users'),
  updateUserRoles: (userId, roles) => api.put(`/admin/users/${userId}/roles`, { roles }),
  toggleUser: (userId, enabled) => api.patch(`/admin/users/${userId}/toggle`, { enabled }),
  deleteUser: (userId) => api.delete(`/admin/users/${userId}`),
};
