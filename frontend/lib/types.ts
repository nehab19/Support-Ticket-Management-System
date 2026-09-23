// Enums matching the backend model
export type TicketStatus = 'OPEN' | 'IN_PROGRESS' | 'RESOLVED' | 'CLOSED' | 'CANCELLED';
export type Priority = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';

// Response shapes
export interface Comment {
  id: number;
  author: string;
  body: string;
  createdAt: string;
}

export interface TicketSummary {
  id: number;
  title: string;
  priority: Priority;
  status: TicketStatus;
  assignee: string | null;
  createdAt: string;
}

export interface Ticket extends TicketSummary {
  description: string;
  updatedAt: string;
  comments: Comment[];
}

export interface FieldError {
  field: string;
  message: string;
}

export interface ErrorResponse {
  status: number;
  message: string;
  errors: FieldError[];
}

// Request payloads
export interface CreateTicketPayload {
  title: string;
  description: string;
  priority?: Priority;
}

export interface UpdateTicketPayload {
  title?: string;
  description?: string;
  priority?: Priority;
  assignee?: string;
}

export interface StatusTransitionPayload {
  status: TicketStatus;
}

export interface CreateCommentPayload {
  author: string;
  body: string;
}

// State machine — valid transitions (mirrors backend TicketStateMachine)
export const VALID_TRANSITIONS: Record<TicketStatus, TicketStatus[]> = {
  OPEN: ['IN_PROGRESS', 'CANCELLED'],
  IN_PROGRESS: ['RESOLVED', 'CANCELLED'],
  RESOLVED: ['CLOSED'],
  CLOSED: [],
  CANCELLED: [],
};
