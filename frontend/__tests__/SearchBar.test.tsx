import React from 'react';
import { render, screen, act } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import SearchBar from '../components/SearchBar';

describe('SearchBar', () => {
  beforeEach(() => {
    jest.useFakeTimers();
  });

  afterEach(() => {
    jest.runOnlyPendingTimers();
    jest.useRealTimers();
  });

  it('calls onSearch with the trimmed value after the debounce delay', async () => {
    const onSearch = jest.fn();
    const user = userEvent.setup({ advanceTimers: jest.advanceTimersByTime.bind(jest) });

    render(<SearchBar onSearch={onSearch} />);
    const input = screen.getByRole('textbox', { name: /search tickets/i });

    // Type into the input — userEvent.setup with advanceTimers handles fake timers
    await user.type(input, '  hello  ');

    // Advance past the 400ms debounce
    act(() => {
      jest.advanceTimersByTime(500);
    });

    // The last debounced call should be with the trimmed value
    const calls = onSearch.mock.calls;
    const lastCall = calls[calls.length - 1];
    expect(lastCall[0]).toBe('hello');
  });

  it('calls onSearch with an empty string when input is cleared', async () => {
    const onSearch = jest.fn();
    const user = userEvent.setup({ advanceTimers: jest.advanceTimersByTime.bind(jest) });

    render(<SearchBar onSearch={onSearch} />);
    const input = screen.getByRole('textbox', { name: /search tickets/i });

    await user.type(input, 'ticket');
    act(() => { jest.advanceTimersByTime(500); });
    onSearch.mockClear();

    await user.clear(input);
    act(() => { jest.advanceTimersByTime(500); });

    expect(onSearch).toHaveBeenCalledWith('');
  });
});
