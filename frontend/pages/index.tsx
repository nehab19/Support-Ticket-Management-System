import React, { useCallback, useEffect, useState } from 'react';
import type { NextPage } from 'next';
import * as api from '../lib/api';
import type { TicketSummary, TicketStatus, Ticket, ErrorResponse } from '../lib/types';
import TicketTable from '../components/TicketTable';
import SearchBar from '../components/SearchBar';
import StatusFilter from '../components/StatusFilter';
import CreateTicketForm from '../components/CreateTicketForm';
import ErrorNotification from '../components/ErrorNotification';

const Home: NextPage = () => {
  const [tickets, setTickets] = useState<TicketSummary[]>([]);
  const [statusFilter, setStatusFilter] = useState<TicketStatus | ''>('');
  const [searchQuery, setSearchQuery] = useState('');
  const [showCreateForm, setShowCreateForm] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);

  const fetchTickets = useCallback(async () => {
    setError(null);
    setLoading(true);
    try {
      const params: { status?: TicketStatus; q?: string } = {};
      if (statusFilter) params.status = statusFilter;
      if (searchQuery) params.q = searchQuery;
      const data = await api.listTickets(params);
      setTickets(data);
    } catch (err) {
      const apiErr = err as ErrorResponse;
      setError(apiErr.message || 'Failed to load tickets');
    } finally {
      setLoading(false);
    }
  }, [statusFilter, searchQuery]);

  useEffect(() => {
    fetchTickets();
  }, [fetchTickets]);

  const handleSearch = useCallback((keyword: string) => {
    setSearchQuery(keyword);
    setStatusFilter(''); // Clear status filter when searching
  }, []);

  const handleStatusChange = useCallback((status: TicketStatus | '') => {
    setStatusFilter(status);
    setSearchQuery(''); // Clear search when filtering
  }, []);

  const handleCreated = useCallback((ticket: Ticket) => {
    setShowCreateForm(false);
    fetchTickets(); // Refresh list
  }, [fetchTickets]);

  return (
    <div style={{ maxWidth: '1200px', margin: '0 auto', padding: '24px' }}>
      <header style={{ marginBottom: '24px' }}>
        <h1 style={{ fontSize: '28px', fontWeight: 700, color: '#111827' }}>
          Support Tickets
        </h1>
      </header>

      <ErrorNotification error={error} />

      <div style={{ display: 'flex', gap: '12px', alignItems: 'center', marginBottom: '20px', flexWrap: 'wrap' }}>
        <SearchBar onSearch={handleSearch} />
        <StatusFilter value={statusFilter} onStatusChange={handleStatusChange} />
        <button
          onClick={() => setShowCreateForm(true)}
          style={{
            marginLeft: 'auto',
            background: '#2563eb',
            color: '#fff',
            border: 'none',
            borderRadius: '6px',
            padding: '8px 20px',
            cursor: 'pointer',
            fontSize: '14px',
            fontWeight: 600,
          }}
        >
          + New Ticket
        </button>
      </div>

      {loading ? (
        <p style={{ textAlign: 'center', color: '#6b7280', padding: '32px' }}>Loading tickets...</p>
      ) : (
        <TicketTable tickets={tickets} />
      )}

      {showCreateForm && (
        <CreateTicketForm
          onCreated={handleCreated}
          onClose={() => setShowCreateForm(false)}
        />
      )}
    </div>
  );
};

export default Home;
