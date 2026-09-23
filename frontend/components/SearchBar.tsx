import React, { useEffect, useState } from 'react';

interface Props {
  onSearch: (keyword: string) => void;
  placeholder?: string;
}

export default function SearchBar({ onSearch, placeholder = 'Search tickets...' }: Props) {
  const [value, setValue] = useState('');

  useEffect(() => {
    const timer = setTimeout(() => {
      onSearch(value.trim());
    }, 400);
    return () => clearTimeout(timer);
  }, [value, onSearch]);

  return (
    <input
      type="text"
      value={value}
      onChange={(e) => setValue(e.target.value)}
      placeholder={placeholder}
      aria-label="Search tickets"
      style={{
        padding: '8px 12px',
        border: '1px solid #d1d5db',
        borderRadius: '6px',
        fontSize: '14px',
        width: '280px',
      }}
    />
  );
}
