import React from 'react';
import type { Comment } from '../lib/types';

interface Props {
  comments: Comment[];
}

export default function CommentList({ comments }: Props) {
  if (comments.length === 0) {
    return <p style={{ color: '#6b7280', fontSize: '14px' }}>No comments yet.</p>;
  }

  return (
    <ul style={{ listStyle: 'none', display: 'flex', flexDirection: 'column', gap: '12px' }}>
      {comments.map((comment) => (
        <li key={comment.id} style={{
          background: '#f9fafb', borderRadius: '8px',
          padding: '12px 16px', borderLeft: '3px solid #2563eb',
        }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '6px' }}>
            <strong style={{ fontSize: '14px' }}>{comment.author}</strong>
            <span style={{ fontSize: '12px', color: '#6b7280' }}>
              {new Date(comment.createdAt).toLocaleString()}
            </span>
          </div>
          <p style={{ fontSize: '14px', color: '#374151', whiteSpace: 'pre-wrap' }}>{comment.body}</p>
        </li>
      ))}
    </ul>
  );
}
