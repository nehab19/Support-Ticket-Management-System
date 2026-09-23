import React, { useCallback, useEffect, useState } from 'react';
import type { NextPage } from 'next';
import { useRouter } from 'next/router';
import Link from 'next/link';
import * as api from '../../lib/api';
import type { Ticket, Comment, ErrorResponse } from '../../lib/types';
import UpdateTicketForm from '../../components/UpdateTicketForm';
import StatusTransitionPanel from '../../components/StatusTransitionPanel';
import CommentList from '../../components/CommentList';
import CommentForm from '../../components/CommentForm';
import ErrorNotification from '../../components/ErrorNotification';

const TicketDetailPage: NextPage = () => {
  const router = useRouter();
  const { id } = router.query;
  const ticketId = typeof id === 'string' ? parseInt(id, 10) : null;

  const [ticket, setTicket] = useState<Ticket | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);

  const fetchTicket = useCallback(async () => {
    if (!ticketId) return;
    setError(null);
    setLoading(true);
    try {
      const data = await api.getTicket(ticketId);
      setTicket(data);
    } catch (err) {
      const apiErr = err as ErrorResponse;
      setError(apiErr.message || 'Failed to load ticket');
    } finally {
      setLoading(false);
    }
  }, [ticketId]);

  useEffect(() => {
    fetchTicket();
  }, [fetchTicket]);

  const handleUpdated = useCallback((updated: Ticket) => {
    setTicket(updated);
  }, []);

  const handleTransitioned = useCallback((updated: Ticket) => {
    setTicket(updated);
  }, []);

  const handleCommentAdded = useCallback((comment: Comment) => {
    setTicket((prev) => prev ? { ...prev, comments: [...prev.comments, comment] } : prev);
  }, []);

  if (loading) {
    return (
      <div style={{ maxWidth: '900px', margin: '0 auto', padding: '24px' }}>
        <p style={{ color: '#6b7280' }}>Loading ticket...</p>
      </div>
    );
  }

  if (!ticket) {
    return (
      <div style={{ maxWidth: '900px', margin: '0 auto', padding: '24px' }}>
        <ErrorNotification error={error} />
        <Link href="/" style={{ color: '#2563eb' }}>← Back to tickets</Link>
      </div>
    );
  }

  return (
    <div style={{ maxWidth: '900px', margin: '0 auto', padding: '24px' }}>
      <div style={{ marginBottom: '16px' }}>
        <Link href="/" style={{ color: '#2563eb', fontSize: '14px' }}>← Back to tickets</Link>
      </div>

      <ErrorNotification error={error} />

      <div style={{ background: '#fff', borderRadius: '8px', padding: '24px', marginBottom: '20px', boxShadow: '0 1px 3px rgba(0,0,0,0.1)' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '16px' }}>
          <h1 style={{ fontSize: '22px', fontWeight: 700, color: '#111827' }}>{ticket.title}</h1>
          <span style={{ fontSize: '12px', color: '#6b7280' }}>#{ticket.id}</span>
        </div>
        <p style={{ fontSize: '14px', color: '#374151', marginBottom: '16px', whiteSpace: 'pre-wrap' }}>
          {ticket.description}
        </p>
        <dl style={{ display: 'grid', gridTemplateColumns: 'repeat(2, 1fr)', gap: '8px', fontSize: '13px', color: '#6b7280' }}>
          <div><dt style={{ fontWeight: 600 }}>Priority</dt><dd>{ticket.priority}</dd></div>
          <div><dt style={{ fontWeight: 600 }}>Assignee</dt><dd>{ticket.assignee ?? 'Unassigned'}</dd></div>
          <div><dt style={{ fontWeight: 600 }}>Created</dt><dd>{new Date(ticket.createdAt).toLocaleString()}</dd></div>
          <div><dt style={{ fontWeight: 600 }}>Updated</dt><dd>{new Date(ticket.updatedAt).toLocaleString()}</dd></div>
        </dl>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '20px', marginBottom: '20px' }}>
        <StatusTransitionPanel ticket={ticket} onTransitioned={handleTransitioned} />
        <UpdateTicketForm ticket={ticket} onUpdated={handleUpdated} />
      </div>

      <div style={{ background: '#fff', borderRadius: '8px', padding: '24px', boxShadow: '0 1px 3px rgba(0,0,0,0.1)' }}>
        <h2 style={{ fontSize: '18px', fontWeight: 600, marginBottom: '16px' }}>
          Comments ({ticket.comments.length})
        </h2>
        <CommentList comments={ticket.comments} />
        <hr style={{ margin: '20px 0', border: 'none', borderTop: '1px solid #e5e7eb' }} />
        <h3 style={{ fontSize: '15px', fontWeight: 600, marginBottom: '8px' }}>Add a comment</h3>
        <CommentForm ticketId={ticket.id} onAdded={handleCommentAdded} />
      </div>
    </div>
  );
};

export default TicketDetailPage;
