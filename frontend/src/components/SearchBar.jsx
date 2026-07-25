import React from 'react';
import { Search, MapPin, Users, Calendar } from 'lucide-react';
import { useNavigate } from 'react-router-dom';

export default function SearchBar({ compact = false }) {
  const [destination, setDestination] = React.useState('');
  const [guests, setGuests] = React.useState(1);
  const navigate = useNavigate();

  const handleSearch = (e) => {
    e.preventDefault();
    const params = new URLSearchParams();
    if (destination) params.set('city', destination);
    if (guests > 1) params.set('guestCount', guests);
    navigate(`/search?${params.toString()}`);
  };

  if (compact) {
    return (
      <form onSubmit={handleSearch} className="flex items-center gap-2 bg-white rounded-full shadow-md px-4 py-2">
        <Search className="w-5 h-5 text-gray-400" />
        <input
          type="text"
          placeholder="Search destinations..."
          value={destination}
          onChange={(e) => setDestination(e.target.value)}
          className="flex-1 bg-transparent outline-none text-sm"
        />
        <button type="submit" className="bg-primary-600 text-white p-2 rounded-full hover:bg-primary-700 transition-colors">
          <Search className="w-4 h-4" />
        </button>
      </form>
    );
  }

  return (
    <form onSubmit={handleSearch} className="bg-white rounded-full shadow-lg border border-gray-100 p-2 flex flex-col md:flex-row items-stretch md:items-center divide-y md:divide-y-0 md:divide-x divide-gray-100">
      <div className="flex-1 px-5 py-3 md:py-0">
        <label className="text-xs font-semibold text-gray-900">Destination</label>
        <div className="flex items-center gap-2 mt-1">
          <MapPin className="w-4 h-4 text-gray-400" />
          <input
            type="text"
            placeholder="Where are you going?"
            value={destination}
            onChange={(e) => setDestination(e.target.value)}
            className="bg-transparent outline-none text-sm w-full placeholder:text-gray-400"
          />
        </div>
      </div>
      <div className="flex-1 px-5 py-3 md:py-0">
        <label className="text-xs font-semibold text-gray-900">Check in / Check out</label>
        <div className="flex items-center gap-2 mt-1">
          <Calendar className="w-4 h-4 text-gray-400" />
          <span className="text-sm text-gray-400">Add dates</span>
        </div>
      </div>
      <div className="flex-1 px-5 py-3 md:py-0 flex items-center gap-2">
        <div className="flex-1">
          <label className="text-xs font-semibold text-gray-900">Guests</label>
          <div className="flex items-center gap-2 mt-1">
            <Users className="w-4 h-4 text-gray-400" />
            <select value={guests} onChange={(e) => setGuests(e.target.value)} className="bg-transparent outline-none text-sm w-full">
              {[1,2,3,4,5,6,7,8].map(n => <option key={n} value={n}>{n} {n === 1 ? 'Guest' : 'Guests'}</option>)}
            </select>
          </div>
        </div>
        <button type="submit" className="bg-primary-600 text-white p-3.5 rounded-full hover:bg-primary-700 transition-colors shrink-0">
          <Search className="w-5 h-5" />
        </button>
      </div>
    </form>
  );
}
