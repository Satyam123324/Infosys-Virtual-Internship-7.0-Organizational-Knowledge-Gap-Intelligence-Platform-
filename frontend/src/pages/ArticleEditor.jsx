import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import Layout from '../components/Layout';
import { ArrowLeft } from 'lucide-react';
import { knowledgeArticleApi } from '../api/knowledgeArticleApi';
import { employeeApi } from '../api/employeeApi';

export default function ArticleEditor() {
  const { id } = useParams();
  const isEditing = Boolean(id);
  const navigate = useNavigate();

  const [skills, setSkills] = useState([]);
  const [form, setForm] = useState({ title: '', summary: '', content: '', skillId: '' });
  const [loading, setLoading] = useState(isEditing);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    employeeApi.getAllSkills().then(({ data }) => setSkills(data.data)).catch(() => {});

    if (isEditing) {
      knowledgeArticleApi.getById(id)
        .then(({ data }) => {
          const a = data.data;
          if (!a.canEdit) {
            setError("You don't have permission to edit this article");
            return;
          }
          setForm({ title: a.title, summary: a.summary || '', content: a.content, skillId: a.skillId || '' });
        })
        .catch(() => setError('Failed to load article'))
        .finally(() => setLoading(false));
    }
  }, [id, isEditing]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSaving(true);
    try {
      const payload = { ...form, skillId: form.skillId || null };
      if (isEditing) {
        await knowledgeArticleApi.update(id, payload);
        navigate(`/knowledge-articles/${id}`);
      } else {
        const { data } = await knowledgeArticleApi.create(payload);
        navigate(`/knowledge-articles/${data.data.id}`);
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to save article');
    } finally {
      setSaving(false);
    }
  };

  return (
    <Layout title="" subtitle="">
      <button
        onClick={() => navigate(-1)}
        style={{ display: 'inline-flex', alignItems: 'center', gap: 6, fontSize: 13.5, color: '#64748b', background: 'none', border: 'none', cursor: 'pointer', marginBottom: 16, padding: 0 }}
      >
        <ArrowLeft size={15} /> Back
      </button>

      <div style={{ fontSize: 22, fontWeight: 800, color: '#0f172a', marginBottom: 16 }}>
        {isEditing ? 'Edit Article' : 'Write an Article'}
      </div>

      {loading ? (
        <div className="loading-text">Loading...</div>
      ) : (
        <form onSubmit={handleSubmit} className="info-card">
          {error && <div className="alert alert-error" style={{ marginBottom: 12 }}>{error}</div>}

          <div className="form-group">
            <label>Title</label>
            <input value={form.title} onChange={(e) => setForm({ ...form, title: e.target.value })} required />
          </div>

          <div style={{ display: 'flex', gap: 12, flexWrap: 'wrap' }}>
            <div className="form-group" style={{ flex: '1 1 300px' }}>
              <label>Summary (optional, shown in the library list)</label>
              <input value={form.summary} onChange={(e) => setForm({ ...form, summary: e.target.value })} placeholder="One-sentence overview" />
            </div>
            <div className="form-group" style={{ flex: '0 1 200px' }}>
              <label>Related Skill (optional)</label>
              <select value={form.skillId} onChange={(e) => setForm({ ...form, skillId: e.target.value })}>
                <option value="">None</option>
                {skills.map((s) => (
                  <option key={s.id} value={s.id}>{s.name}</option>
                ))}
              </select>
            </div>
          </div>

          <div className="form-group">
            <label>Content</label>
            <textarea
              rows={14}
              value={form.content}
              onChange={(e) => setForm({ ...form, content: e.target.value })}
              placeholder="Write your article here — plain text, paragraphs preserved."
              required
            />
          </div>

          <button type="submit" className="btn-primary" style={{ width: 'auto', padding: '10px 22px' }} disabled={saving}>
            {saving ? 'Publishing...' : isEditing ? 'Save Changes' : 'Publish Article'}
          </button>
        </form>
      )}
    </Layout>
  );
}
