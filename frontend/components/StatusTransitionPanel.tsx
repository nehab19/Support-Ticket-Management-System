import React, { useState } from 'react';
import * as api from '../lib/api';
import type { Ticket, TicketStatus, ErrorResponse } from '../lib/types';
import { VALID_TRANSITIONS } from '../lib/types';
import ErrorNotification from './ErrorNotification';

interface Props {
  ticket: Ticket;
  onTransitioned: (ticket: Ticket) => void;
}

const STATUS_LABELS: Record<TicketStatus, string> = {
  OPEN: 'Open',
  IN_PROGRESS: 'In Progress',
  RESOLVED: 'Resolved',
  CLOSED: 'Closed',
  CANCELLED: 'Cancelled',
};

export default function StatusTransitionPanel({ ticket, onTransitioned }: Props) {
  const [error, setError] = useState<string | null>(null);
  const [transitioning, setTransitioning] = useState<TicketStatus | null>(null);

  const validNextStatuses = VALID_TRANSITIONS[ticket.status];

  const handleTransition = async (toStatus: TicketStatus) => {
    setError(null);
    setTransitioning(toStatus);
    try {
      const updated = await api.transitionStatus(ticket.id, { status: toStatus });
      onTransitioned(updated);
    } catch (err) {
      const apiErr = err as ErrorResponse;
      setError(apiErr.message || 'Status transition failed');
    } finally {
      setTransitioning(null);
    }
  };

  return (
    <div style={{ background: '#f9fafb', borderRadius: '8px', padding: '16px' }}>
      <h3 style={{ marginBottom: '12px', fontSize: '16px' }}>
        Status: <strong>{STATUS_LABELS[ticket.status]}</strong>
      </h3>
      <ErrorNotification error={error} />
      {validNextStatuses.length === 0 ? (
        <p style={{ color: '#6b7280', fontSize: '14px' }}>No further transitions available.</p>
      ) : (
        <div style={{ display: 'flex', gap: '8px', flexWrap: 'wrap' }}>
          {validNextStatuses.map((nextStatus) => (
            <button
              key={nextStatus}
              onClick={() => handleTransition(nextStatus)}
              disabled={transitioning !== null}
              aria-label={`Transition to ${STATUS_LABELS[nextStatus]}`}
              style={{
                background: '#2563eb', color: '#fff', border: 'none',
                borderRadius: '6px', padding: '8px 16px', cursor: 'pointer', fontSize: '14px',
              }}
            >
              {transitioning === nextStatus ? 'Updating...' : `→ ${STATUS_LABELS[nextStatus]}`}
            </button>
          ))}
        </div>
      )}
    </div>
  );
}
