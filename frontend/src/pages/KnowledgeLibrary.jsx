import { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import Layout from '../components/Layout';
import { Search, BookOpen, Plus, Eye, User as UserIcon } from 'lucide-react';
import { knowledgeArticleApi } from '../api/knowledgeArticleApi';
import { employeeApi } from '../api/employeeApi';

export default function KnowledgeLibrary() {
  const navigate = useNavigate();
  const [articles, setArticles] = useState([]);
  const [skills, setSkills] = useState([]);
  const [search, setSearch] = useState('');
  const [skillFilter, setSkillFilter] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const load = async (params = {}) => {
    setLoading(true);
    setError('');
    try {
      const { data } = await knowledgeArticleApi.getAll(params);
      setArticles(data.data);
    } catch (err) {
      setError('Failed to load articles');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load();
    employeeApi.getAllSkills().then(({ data }) => setSkills(data.data)).catch(() => {});
  }, []);

  const handleSearch = (e) => {
    e.preventDefault();
    setSkillFilter('');
    load(search ? { search } : {});
  };

  const handleSkillFilter = (skillId) => {
    setSkillFilter(skillId);
    setSearch('');
    load(skillId ? { skillId } : {});
  };

  return (
    <Layout
      title="Knowledge Article Library"
      subtitle="Written by your colleagues — search by topic or browse by skill"
    >
      <div style={{ display: 'flex', justifyContent: 'space-between', gap: 12, marginBottom: 16, flexWrap: 'wrap' }}>
        <form onSubmit={handleSearch} style={{ display: 'flex', gap: 8, flex: 1, maxWidth: 420 }}>
          <div className="auth-input-icon-group" style={{ flex: 1 }}>
            <Search size={15} />
            <input value={search} onChange={(e) => setSearch(e.target.value)} placeholder="Search articles..." />
          </div>
          <button type="submit" className="btn-sm">Search</button>
        </form>
        <button className="btn-primary" style={{ width: 'auto', padding: '9px 18px', display: 'flex', alignItems: 'center', gap: 6 }}
                onClick={() => navigate('/knowledge-articles/new')}>
          <Plus size={15} /> Write an Article
        </button>
      </div>

      <div style={{ display: 'flex', gap: 6, marginBottom: 20, flexWrap: 'wrap' }}>
        <button
          onClick={() => handleSkillFilter('')}
          className="status-pill"
          style={{
            border: '1px solid ' + (!skillFilter ? '#0d9488' : '#e2e8f0'),
            background: !skillFilter ? '#0d9488' : 'white',
            color: !skillFilter ? 'white' : '#475569',
            cursor: 'pointer', fontSize: 12.5,
          }}
        >
          All Topics
        </button>
        {skills.map((s) => (
          <button
            key={s.id}
            onClick={() => handleSkillFilter(s.id)}
            className="status-pill"
            style={{
              border: '1px solid ' + (String(skillFilter) === String(s.id) ? '#0d9488' : '#e2e8f0'),
              background: String(skillFilter) === String(s.id) ? '#0d9488' : 'white',
              color: String(skillFilter) === String(s.id) ? 'white' : '#475569',
              cursor: 'pointer', fontSize: 12.5,
            }}
          >
            {s.name}
          </button>
        ))}
      </div>

      {error && <div className="alert alert-error">{error}</div>}

      {loading ? (
        <div className="loading-text">Loading articles...</div>
      ) : articles.length === 0 ? (
        <div className="info-card" style={{ textAlign: 'center', padding: '32px 20px', color: '#94a3b8' }}>
          <BookOpen size={26} style={{ marginBottom: 10, opacity: 0.6 }} />
          <div style={{ fontSize: 13.5 }}>
            {search || skillFilter ? 'No articles match that.' : 'No articles yet — be the first to write one.'}
          </div>
        </div>
      ) : (
        <div className="card-grid">
          {articles.map((a) => (
            <Link key={a.id} to={`/knowledge-articles/${a.id}`} className="info-card" style={{ textDecoration: 'none', color: 'inherit', display: 'block' }}>
              <div style={{ display: 'flex', gap: 8, alignItems: 'center', marginBottom: 6, flexWrap: 'wrap' }}>
                {a.skillName && <span className="role-badge">{a.skillName}</span>}
              </div>
              <div style={{ fontWeight: 700, fontSize: 15, color: '#0f172a', marginBottom: 6, lineHeight: 1.3 }}>{a.title}</div>
              {a.summary && (
                <div style={{ fontSize: 13, color: '#64748b', marginBottom: 10, lineHeight: 1.5, display: '-webkit-box', WebkitLineClamp: 2, WebkitBoxOrient: 'vertical', overflow: 'hidden' }}>
                  {a.summary}
                </div>
              )}
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', fontSize: 12, color: '#94a3b8' }}>
                <span style={{ display: 'flex', alignItems: 'center', gap: 4 }}>
                  <UserIcon size={12} /> {a.authorName}
                </span>
                <span style={{ display: 'flex', alignItems: 'center', gap: 4 }}>
                  <Eye size={12} /> {a.viewCount}
                </span>
              </div>
            </Link>
          ))}
        </div>
      )}
    </Layout>
  );
}
