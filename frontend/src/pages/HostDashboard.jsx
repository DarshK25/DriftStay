import React from 'react';
import { Link } from 'react-router-dom';
import { BarChart3, Calendar, DollarSign, Users, Home, Eye, Star, TrendingUp } from 'lucide-react';

const STATS = [
  { icon: Eye, label: 'Profile Views', value: '2,847', change: '+12%' },
  { icon: Calendar, label: 'Bookings', value: '18', change: '+8%' },
  { icon: DollarSign, label: 'Revenue', value: '₹2.4L', change: '+15%' },
  { icon: Star, label: 'Avg. Rating', value: '4.8', change: '+0.2' },
];

const BOOKINGS = [
  { guest: 'Rahul S.', property: 'Sea View Villa', checkIn: 'Apr 15', status: 'UPCOMING', amount: '₹12,500' },
  { guest: 'Priya M.', property: 'Mountain Retreat', checkIn: 'Apr 20', status: 'PENDING', amount: '₹8,500' },
];

export default function HostDashboard() {
  return (
    <div className="pt-24 pb-16 bg-gray-50 min-h-screen">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between mb-8">
          <div>
            <h1 className="text-3xl font-display font-bold text-gray-900">Host Dashboard</h1>
            <p className="text-gray-500 mt-1">Manage your properties and bookings</p>
          </div>
          <Link to="/host/properties/new"
            className="bg-primary-600 text-white px-5 py-2.5 rounded-full font-medium hover:bg-primary-700 transition-colors flex items-center gap-2">
            <Home className="w-4 h-4" /> Add Property
          </Link>
        </div>

        <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-8">
          {STATS.map((s, i) => (
            <div key={i} className="bg-white rounded-2xl p-6 shadow-sm border border-gray-100">
              <div className="flex items-center gap-3 mb-3">
                <div className="w-10 h-10 rounded-xl bg-primary-50 flex items-center justify-center">
                  <s.icon className="w-5 h-5 text-primary-600" />
                </div>
                <span className="text-sm text-gray-500">{s.label}</span>
              </div>
              <p className="text-2xl font-bold text-gray-900">{s.value}</p>
              <p className="text-xs text-green-600">{s.change} vs last month</p>
            </div>
          ))}
        </div>

        <div className="bg-white rounded-2xl shadow-sm border border-gray-100 overflow-hidden">
          <div className="px-6 py-4 border-b border-gray-100 flex items-center justify-between">
            <h2 className="font-semibold text-gray-900">Recent Bookings</h2>
            <TrendingUp className="w-5 h-5 text-gray-400" />
          </div>
          <div className="divide-y divide-gray-50">
            {BOOKINGS.map((b, i) => (
              <div key={i} className="px-6 py-4 flex items-center justify-between">
                <div>
                  <p className="font-medium text-gray-900">{b.guest}</p>
                  <p className="text-sm text-gray-500">{b.property} • {b.checkIn}</p>
                </div>
                <div className="text-right">
                  <p className="font-medium">{b.amount}</p>
                  <span className={`text-xs font-medium px-2 py-0.5 rounded-full ${
                    b.status === 'CONFIRMED' ? 'bg-green-100 text-green-700' :
                    b.status === 'UPCOMING' ? 'bg-blue-100 text-blue-700' :
                    b.status === 'PENDING' ? 'bg-amber-100 text-amber-700' :
                    'bg-gray-100 text-gray-700'
                  }`}>{b.status}</span>
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}
