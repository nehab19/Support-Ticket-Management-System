import type {
  Ticket,
  TicketSummary,
  Comment,
  ErrorResponse,
  CreateTicketPayload,
  UpdateTicketPayload,
  StatusTransitionPayload,
  CreateCommentPayload,
  TicketStatus,
} from './types';

const API_BASE = process.env.NEXT_PUBLIC_API_BASE_URL || 'http://localhost:8080';

/**
 * Generic fetch wrapper that throws an ErrorResponse on non-OK responses.
 */
async function apiFetch<T>(path: string, init?: RequestInit): Promise<T> {
  const res = await fetch(`${API_BASE}${path}`, {
    headers: { 'Content-Type': 'application/json', ...init?.headers },
    ...init,
  });

  if (!res.ok) {
    let errorBody: ErrorResponse;
    try {
      errorBody = await res.json();
    } catch {
      errorBody = { status: res.status, message: res.statusText, errors: [] };
    }
    throw errorBody;
  }

  // Handle 204 No Content or empty responses
  const text = await res.text();
  return text ? (JSON.parse(text) as T) : ({} as T);
}

// --- Tickets ---

export function listTickets(params?: { status?: TicketStatus; q?: string }): Promise<TicketSummary[]> {
  const qs = new URLSearchParams();
  if (params?.status) qs.set('status', params.status);
  if (params?.q) qs.set('q', params.q);
  const query = qs.toString() ? `?${qs.toString()}` : '';
  return apiFetch<TicketSummary[]>(`/api/tickets${query}`);
}

export function getTicket(id: number): Promise<Ticket> {
  return apiFetch<Ticket>(`/api/tickets/${id}`);
}

export function createTicket(payload: CreateTicketPayload): Promise<Ticket> {
  return apiFetch<Ticket>('/api/tickets', {
    method: 'POST',
    body: JSON.stringify(payload),
  });
}

export function updateTicket(id: number, payload: UpdateTicketPayload): Promise<Ticket> {
  return apiFetch<Ticket>(`/api/tickets/${id}`, {
    method: 'PATCH',
    body: JSON.stringify(payload),
  });
}

export function transitionStatus(id: number, payload: StatusTransitionPayload): Promise<Ticket> {
  return apiFetch<Ticket>(`/api/tickets/${id}/status`, {
    method: 'PATCH',
    body: JSON.stringify(payload),
  });
}

// --- Comments ---

export function addComment(ticketId: number, payload: CreateCommentPayload): Promise<Comment> {
  return apiFetch<Comment>(`/api/tickets/${ticketId}/comments`, {
    method: 'POST',
    body: JSON.stringify(payload),
  });
}
