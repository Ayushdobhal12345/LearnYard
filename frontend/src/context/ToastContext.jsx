import { createContext, useContext, useState, useCallback } from 'react';

const ToastContext = createContext(null);

export function ToastProvider({ children }) {
  const [toasts, setToasts] = useState([]);

  const showToast = useCallback((message, tone = 'default') => {
    const id = Date.now() + Math.random();
    setToasts((prev) => [...prev, { id, message, tone }]);
    setTimeout(() => setToasts((prev) => prev.filter((t) => t.id !== id)), 4000);
  }, []);

  return (
    <ToastContext.Provider value={showToast}>
      {children}
      <div style={{ position: 'fixed', bottom: 24, right: 24, display: 'flex', flexDirection: 'column', gap: 10, zIndex: 1000 }}>
        {toasts.map((t) => (
          <div
            key={t.id}
            style={{
              padding: '13px 20px', borderRadius: 12, fontSize: 13.5, fontWeight: 600,
              color: t.tone === 'error' ? '#fff' : 'var(--dark)',
              background: t.tone === 'error' ? '#c0392b' : 'var(--lime)',
              boxShadow: '0 8px 24px rgba(0,0,0,0.18)', minWidth: 220,
            }}
          >
            {t.message}
          </div>
        ))}
      </div>
    </ToastContext.Provider>
  );
}

export function useToast() {
  return useContext(ToastContext);
}