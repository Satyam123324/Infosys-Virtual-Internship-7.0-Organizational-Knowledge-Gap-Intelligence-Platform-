import api from './axios';

export const knowledgeArticleApi = {
  getAll: (params) => api.get('/knowledge-articles', { params }),
  getById: (id) => api.get(`/knowledge-articles/${id}`),
  create: (data) => api.post('/knowledge-articles', data),
  update: (id, data) => api.put(`/knowledge-articles/${id}`, data),
  delete: (id) => api.delete(`/knowledge-articles/${id}`),
};
