import api from './axios';

export const adminApi = {
  getAllUsers: () => api.get('/admin/users'),
};
