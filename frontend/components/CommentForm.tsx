import React, { useState } from 'react';
import * as api from '../lib/api';
import type { Comment, ErrorResponse } from '../lib/types';
import ErrorNotification from './ErrorNotification';

interface Props {
  ticketId: number;
  onAdded: (comment: Comment) => void;
}

export default function CommentForm({ ticketId, onAdded }: Props) {
  const [author, setAuthor] = useState('');
  const [body, setBody] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setSubmitting(true);
    try {
      const comment = await api.addComment(ticketId, { author, body });
      setAuthor('');
      setBody('');
      onAdded(comment);
    } catch (err) {
      const apiErr = err as ErrorResponse;
      setError(apiErr.message || 'Failed to add comment');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} style={{ marginTop: '16px' }}>
      <ErrorNotification error={error} />
      <label style={labelStyle}>
        Your name *
        <input
          value={author}
          onChange={(e) => setAuthor(e.target.value)}
          required
          style={inputStyle}
          placeholder="Your name"
        />
      </label>
      <label style={labelStyle}>
        Comment *
        <textarea
          value={body}
          onChange={(e) => setBody(e.target.value)}
          required
          rows={3}
          style={{ ...inputStyle, resize: 'vertical' }}
          placeholder="Write a comment..."
        />
      </label>
      <button type="submit" disabled={submitting} style={btnStyle}>
        {submitting ? 'Posting...' : 'Add Comment'}
      </button>
    </form>
  );
}

const labelStyle: React.CSSProperties = {
  display: 'flex', flexDirection: 'column', gap: '4px',
  marginBottom: '12px', fontSize: '14px', fontWeight: 500, color: '#374151',
};
const inputStyle: React.CSSProperties = {
  padding: '8px 12px', border: '1px solid #d1d5db',
  borderRadius: '6px', fontSize: '14px', width: '100%',
};
const btnStyle: React.CSSProperties = {
  background: '#2563eb', color: '#fff', border: 'none',
  borderRadius: '6px', padding: '8px 20px', cursor: 'pointer', fontSize: '14px',
};
