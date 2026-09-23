import React, { useState } from 'react';

interface Props {
  error: string | null;
}

export default function ErrorNotification({ error }: Props) {
  const [dismissed, setDismissed] = useState(false);

  if (!error || dismissed) return null;

  return (
    <div
      role="alert"
      style={{
        background: '#fee2e2',
        border: '1px solid #ef4444',
        borderRadius: '6px',
        padding: '12px 16px',
        marginBottom: '16px',
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center',
        color: '#b91c1c',
      }}
    >
      <span>{error}</span>
      <button
        onClick={() => setDismissed(true)}
        aria-label="Dismiss error"
        style={{ background: 'none', border: 'none', cursor: 'pointer', fontSize: '18px', color: '#b91c1c' }}
      >
        ×
      </button>
    </div>
  );
}
