import api from './axios';

export const knowledgeArticleApi = {
  create: (data) => api.post('/knowledge-articles', data),
  getAll: (search, skillId) => api.get('/knowledge-articles', { params: { search: search || undefined, skillId: skillId || undefined } }),
  getById: (id) => api.get(`/knowledge-articles/${id}`),
  update: (id, data) => api.put(`/knowledge-articles/${id}`, data),
  delete: (id) => api.delete(`/knowledge-articles/${id}`),
};
