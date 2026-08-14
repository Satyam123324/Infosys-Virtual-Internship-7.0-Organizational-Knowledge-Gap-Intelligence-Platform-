import api from './axios';

export const frameworkApi = {
  getAll: () => api.get('/competency-frameworks'),
  getById: (id) => api.get(`/competency-frameworks/${id}`),
  getByRoleTitle: (roleTitle) => api.get(`/competency-frameworks/by-role/${encodeURIComponent(roleTitle)}`),
  createOrUpdate: (data) => api.post('/competency-frameworks', data),
  delete: (id) => api.delete(`/competency-frameworks/${id}`),
};
