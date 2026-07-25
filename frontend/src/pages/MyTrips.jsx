import React from 'react';
import { Link } from 'react-router-dom';
import { Calendar, MapPin, ChevronRight } from 'lucide-react';
import { formatDate } from '../lib/utils';

const SAMPLE_TRIPS = [
  { id: 1, ref: 'DRF-2026-00001', name: 'Sea View Villa', city: 'Goa', checkIn: '2026-04-15', checkOut: '2026-04-18', status: 'CONFIRMED', total: 12500, image: 'https://images.unsplash.com/photo-1564013799919-ab600027ffc6?w=400' },
  { id: 2, ref: 'DRF-2026-00002', name: 'Mountain Retreat', city: 'Manali', checkIn: '2026-06-10', checkOut: '2026-06-14', status: 'PENDING', total: 8500, image: 'https://images.unsplash.com/photo-1571896349842-33c89424de2d?w=400' },
];

const STATUS_COLORS = { CONFIRMED: 'bg-green-100 text-green-800', PENDING: 'bg-amber-100 text-amber-800', COMPLETED: 'bg-blue-100 text-blue-800', CANCELLED: 'bg-red-100 text-red-800' };

export default function MyTrips() {
  return (
    <div className="pt-24 pb-16">
      <div className="max-w-4xl mx-auto px-4">
        <h1 className="text-3xl font-display font-bold text-gray-900 mb-2">My Trips</h1>
        <p className="text-gray-500 mb-8">View and manage your bookings</p>

        {SAMPLE_TRIPS.length === 0 ? (
          <div className="text-center py-20 bg-gray-50 rounded-2xl">
            <Calendar className="w-12 h-12 text-gray-300 mx-auto mb-4" />
            <p className="text-gray-500 text-lg">No trips yet</p>
            <Link to="/search" className="mt-4 inline-block text-primary-600 font-medium">Browse properties</Link>
          </div>
        ) : (
          <div className="space-y-4">
            {SAMPLE_TRIPS.map((trip) => (
              <div key={trip.id} className="bg-white rounded-2xl shadow-sm border border-gray-100 overflow-hidden card-hover">
                <Link to={`/bookings/${trip.id}`} className="flex flex-col md:flex-row">
                  <div className="md:w-48 h-48 md:h-auto">
                    <img src={trip.image} alt={trip.name} className="w-full h-full object-cover" />
                  </div>
                  <div className="flex-1 p-6">
                    <div className="flex items-start justify-between mb-3">
                      <div>
                        <h3 className="font-semibold text-gray-900 text-lg">{trip.name}</h3>
                        <p className="text-sm text-gray-500 flex items-center gap-1"><MapPin className="w-3.5 h-3.5" /> {trip.city}</p>
                      </div>
                      <span className={`px-3 py-1 rounded-full text-xs font-semibold ${STATUS_COLORS[trip.status]}`}>{trip.status}</span>
                    </div>
                    <div className="flex items-center gap-4 text-sm text-gray-500">
                      <span>{formatDate(trip.checkIn)} - {formatDate(trip.checkOut)}</span>
                      <span className="font-semibold text-gray-900">₹{trip.total.toLocaleString()}</span>
                    </div>
                  </div>
                  <div className="flex items-center pr-6"><ChevronRight className="w-5 h-5 text-gray-300" /></div>
                </Link>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
