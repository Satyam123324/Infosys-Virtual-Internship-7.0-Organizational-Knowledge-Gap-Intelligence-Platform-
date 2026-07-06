import Sidebar from './Sidebar';

export default function Layout({ title, subtitle, children }) {
  return (
    <div className="shell">
      <Sidebar />
      <div className="main">
        <div className="topbar">
          <div className="topbar-title">
            Organizational Knowledge Gap Intelligence Platform
          </div>
        </div>
        <div className="page-content">
          {(title || subtitle) && (
            <div className="page-header">
              {title && <h1>{title}</h1>}
              {subtitle && <p>{subtitle}</p>}
            </div>
          )}
          {children}
        </div>
      </div>
    </div>
  );
}
