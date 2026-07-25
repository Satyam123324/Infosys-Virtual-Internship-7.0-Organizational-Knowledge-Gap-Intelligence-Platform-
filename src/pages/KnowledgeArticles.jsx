import { useEffect, useState } from 'react';
import { Search, Plus, Eye, Pencil, Trash2, X } from 'lucide-react';
import Layout from '../components/Layout';
import { knowledgeArticleApi } from '../api/knowledgeArticleApi';
import { employeeApi } from '../api/employeeApi';

export default function KnowledgeArticles() {
  const [articles, setArticles] = useState([]);
  const [skills, setSkills] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const [search, setSearch] = useState('');
  const [skillFilter, setSkillFilter] = useState('');
  const [expandedId, setExpandedId] = useState(null);

  const [showForm, setShowForm] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [title, setTitle] = useState('');
  const [summary, setSummary] = useState('');
  const [content, setContent] = useState('');
  const [skillId, setSkillId] = useState('');
  const [saving, setSaving] = useState(false);

  const loadArticles = async (searchText, skill) => {
    try {
      const res = await knowledgeArticleApi.getAll(searchText, skill);
      setArticles(res.data.data);
    } catch (err) {
      setError('Failed to load articles');
    }
  };

  const loadAll = async () => {
    setLoading(true);
    try {
      const [skillsRes] = await Promise.all([employeeApi.getAllSkills(), loadArticles()]);
      setSkills(skillsRes.data.data);
    } catch (err) {
      setError('Failed to load knowledge library');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadAll();
  }, []);

  const handleSearch = (e) => {
    e.preventDefault();
    loadArticles(search.trim() || undefined, skillFilter || undefined);
  };

  const resetForm = () => {
    setEditingId(null);
    setTitle('');
    setSummary('');
    setContent('');
    setSkillId('');
    setShowForm(false);
  };

  const startEdit = (article) => {
    setEditingId(article.id);
    setTitle(article.title);
    setSummary(article.summary || '');
    setContent(article.content);
    setSkillId(article.skillId || '');
    setShowForm(true);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!title.trim() || !content.trim()) return;
    setSaving(true);
    setError('');
    setSuccess('');
    const payload = { title, summary: summary || undefined, content, skillId: skillId ? Number(skillId) : undefined };
    try {
      if (editingId) {
        await knowledgeArticleApi.update(editingId, payload);
        setSuccess('Article updated!');
      } else {
        await knowledgeArticleApi.create(payload);
        setSuccess('Article published!');
      }
      resetForm();
      loadArticles(search.trim() || undefined, skillFilter || undefined);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to save article');
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async (id) => {
    if (!confirm('Delete this article?')) return;
    try {
      await knowledgeArticleApi.delete(id);
      loadArticles(search.trim() || undefined, skillFilter || undefined);
    } catch (err) {
      setError('Failed to delete article');
    }
  };

  const toggleExpand = async (article) => {
    if (expandedId === article.id) {
      setExpandedId(null);
      return;
    }
    setExpandedId(article.id);
    try {
      const res = await knowledgeArticleApi.getById(article.id);
      setArticles((prev) => prev.map((a) => (a.id === article.id ? res.data.data : a)));
    } catch (err) {
      // view count bump failed silently — content is already shown from the list response
    }
  };

  return (
    <Layout title="Knowledge Article Library" subtitle="Browse and share knowledge articles contributed by your colleagues">
      {error && <div className="alert alert-error">{error}</div>}
      {success && <div className="alert alert-success">{success}</div>}

      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: 12, marginBottom: 16 }}>
        <form onSubmit={handleSearch} style={{ display: 'flex', gap: 8, flexWrap: 'wrap', flex: 1 }}>
          <input
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder="Search articles..."
            style={{ flex: 1, minWidth: 200, padding: '9px 12px', borderRadius: 8, border: '1px solid #cbd5e1', fontSize: 13 }}
          />
          <select
            value={skillFilter}
            onChange={(e) => setSkillFilter(e.target.value)}
            style={{ padding: '9px 12px', borderRadius: 8, border: '1px solid #cbd5e1', fontSize: 13 }}
          >
            <option value="">All skills</option>
            {skills.map((s) => <option key={s.id} value={s.id}>{s.name}</option>)}
          </select>
          <button type="submit" className="btn-sm" style={{ display: 'flex', alignItems: 'center', gap: 4 }}>
            <Search size={14} /> Search
          </button>
        </form>
        <button
          className="btn-primary"
          style={{ width: 'auto', padding: '10px 20px', display: 'flex', alignItems: 'center', gap: 6 }}
          onClick={() => (showForm ? resetForm() : setShowForm(true))}
        >
          {showForm ? <><X size={15} /> Cancel</> : <><Plus size={15} /> New Article</>}
        </button>
      </div>

      {showForm && (
        <div className="info-card" style={{ marginBottom: 24 }}>
          <div className="section-title" style={{ marginTop: 0 }}>{editingId ? 'Edit Article' : 'Publish a New Article'}</div>
          <form onSubmit={handleSubmit}>
            <div style={{ display: 'flex', gap: 12, flexWrap: 'wrap', marginBottom: 12 }}>
              <div className="form-group" style={{ marginBottom: 0, flex: 2, minWidth: 220 }}>
                <label>Title</label>
                <input value={title} onChange={(e) => setTitle(e.target.value)} required />
              </div>
              <div className="form-group" style={{ marginBottom: 0, flex: 1, minWidth: 180 }}>
                <label>Related Skill (optional)</label>
                <select value={skillId} onChange={(e) => setSkillId(e.target.value)}>
                  <option value="">None</option>
                  {skills.map((s) => <option key={s.id} value={s.id}>{s.name}</option>)}
                </select>
              </div>
            </div>
            <div className="form-group">
              <label>Summary (optional)</label>
              <input value={summary} onChange={(e) => setSummary(e.target.value)} placeholder="One-line summary" />
            </div>
            <div className="form-group">
              <label>Content</label>
              <textarea
                value={content}
                onChange={(e) => setContent(e.target.value)}
                rows={8}
                required
                style={{ width: '100%', padding: '10px 12px', borderRadius: 8, border: '1px solid #cbd5e1', fontSize: 13, fontFamily: 'inherit', resize: 'vertical' }}
              />
            </div>
            <button type="submit" className="btn-primary" style={{ width: 'auto', padding: '10px 24px' }} disabled={saving}>
              {saving ? 'Saving...' : editingId ? 'Update Article' : 'Publish Article'}
            </button>
          </form>
        </div>
      )}

      <div className="section-title">Articles ({articles.length})</div>
      {loading ? (
        <div className="loading-text">Loading...</div>
      ) : articles.length === 0 ? (
        <div className="info-card" style={{ color: '#64748b' }}>No articles found.</div>
      ) : (
        articles.map((a) => (
          <div key={a.id} className="info-card" style={{ marginBottom: 10 }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', gap: 8, flexWrap: 'wrap' }}>
              <div>
                <span style={{ fontWeight: 600, fontSize: 15 }}>{a.title}</span>
                {a.skillName && <span className="role-badge" style={{ marginLeft: 8 }}>{a.skillName}</span>}
                <div style={{ fontSize: 12, color: '#64748b', marginTop: 4 }}>
                  by {a.authorName} · {new Date(a.createdAt).toLocaleDateString()} · <Eye size={11} style={{ verticalAlign: -1 }} /> {a.viewCount} views
                </div>
                {a.summary && <div style={{ fontSize: 13, color: '#334155', marginTop: 6 }}>{a.summary}</div>}
              </div>
              <div style={{ display: 'flex', gap: 6, flexShrink: 0 }}>
                <button className="btn-sm" onClick={() => toggleExpand(a)}>
                  {expandedId === a.id ? 'Hide' : 'Read'}
                </button>
                {a.canEdit && (
                  <>
                    <button className="btn-sm" onClick={() => startEdit(a)}><Pencil size={14} /></button>
                    <button className="btn-sm" onClick={() => handleDelete(a.id)} style={{ color: '#e11d48' }}><Trash2 size={14} /></button>
                  </>
                )}
              </div>
            </div>
            {expandedId === a.id && (
              <div style={{ marginTop: 12, paddingTop: 12, borderTop: '1px solid #e2e8f0', fontSize: 13, color: '#334155', whiteSpace: 'pre-wrap' }}>
                {a.content}
              </div>
            )}
          </div>
        ))
      )}
    </Layout>
  );
}
