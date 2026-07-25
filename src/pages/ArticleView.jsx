import { useEffect, useState } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import Layout from '../components/Layout';
import { ArrowLeft, Edit2, Trash2, Eye, User as UserIcon, Calendar } from 'lucide-react';
import { knowledgeArticleApi } from '../api/knowledgeArticleApi';

export default function ArticleView() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [article, setArticle] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    knowledgeArticleApi.getById(id)
      .then(({ data }) => setArticle(data.data))
      .catch(() => setError('Article not found'))
      .finally(() => setLoading(false));
  }, [id]);

  const handleDelete = async () => {
    if (!window.confirm('Delete this article? This cannot be undone.')) return;
    try {
      await knowledgeArticleApi.delete(id);
      navigate('/knowledge-articles');
    } catch (err) {
      setError('Failed to delete article');
    }
  };

  return (
    <Layout title="" subtitle="">
      <Link to="/knowledge-articles" style={{ display: 'inline-flex', alignItems: 'center', gap: 6, fontSize: 13.5, color: '#64748b', textDecoration: 'none', marginBottom: 16 }}>
        <ArrowLeft size={15} /> Back to Library
      </Link>

      {loading ? (
        <div className="loading-text">Loading article...</div>
      ) : error ? (
        <div className="alert alert-error">{error}</div>
      ) : article && (
        <div className="info-card">
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', gap: 12, flexWrap: 'wrap' }}>
            <div>
              {article.skillName && <span className="role-badge" style={{ marginBottom: 10, display: 'inline-block' }}>{article.skillName}</span>}
              <h1 style={{ fontSize: 24, fontWeight: 800, color: '#0f172a', margin: '0 0 10px', lineHeight: 1.3 }}>{article.title}</h1>
              <div style={{ display: 'flex', gap: 16, fontSize: 13, color: '#64748b', flexWrap: 'wrap' }}>
                <span style={{ display: 'flex', alignItems: 'center', gap: 5 }}><UserIcon size={13} /> {article.authorName}</span>
                <span style={{ display: 'flex', alignItems: 'center', gap: 5 }}><Calendar size={13} /> {new Date(article.createdAt).toLocaleDateString()}</span>
                <span style={{ display: 'flex', alignItems: 'center', gap: 5 }}><Eye size={13} /> {article.viewCount} views</span>
              </div>
            </div>
            {article.canEdit && (
              <div style={{ display: 'flex', gap: 6, flexShrink: 0 }}>
                <button className="btn-sm" onClick={() => navigate(`/knowledge-articles/${id}/edit`)} style={{ display: 'flex', alignItems: 'center', gap: 4 }}>
                  <Edit2 size={13} /> Edit
                </button>
                <button className="btn-sm" onClick={handleDelete} style={{ display: 'flex', alignItems: 'center', gap: 4 }}>
                  <Trash2 size={13} /> Delete
                </button>
              </div>
            )}
          </div>

          <div style={{ height: 1, background: '#f1f5f9', margin: '18px 0' }} />

          <div style={{ fontSize: 14.5, color: '#334155', lineHeight: 1.8, whiteSpace: 'pre-wrap' }}>
            {article.content}
          </div>
        </div>
      )}
    </Layout>
  );
}
