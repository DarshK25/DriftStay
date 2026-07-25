import React from 'react';
import { Link } from 'react-router-dom';
import { BarChart3, Users, Home, DollarSign, TrendingUp, Activity } from 'lucide-react';

const STATS = [
  { icon: Users, label: 'Total Users', value: '2,847', change: '+156' },
  { icon: Home, label: 'Properties', value: '342', change: '+12' },
  { icon: DollarSign, label: 'Revenue', value: '₹48.2L', change: '+18%' },
  { icon: Activity, label: 'Bookings', value: '1,245', change: '+22%' },
];

export default function AdminDashboard() {
  return (
    <div className="pt-24 pb-16 bg-gray-50 min-h-screen">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="mb-8">
          <h1 className="text-3xl font-display font-bold text-gray-900">Admin Dashboard</h1>
          <p className="text-gray-500 mt-1">Platform overview and management</p>
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
              <p className="text-xs text-green-600">{s.change} this month</p>
            </div>
          ))}
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6">
            <h2 className="font-semibold text-gray-900 mb-4">Quick Actions</h2>
            <div className="space-y-3">
              {['Manage Users', 'Manage Properties', 'View Reports', 'System Settings'].map((action) => (
                <button key={action} className="w-full text-left px-4 py-3 rounded-xl hover:bg-gray-50 transition-colors text-sm font-medium text-gray-700">
                  {action}
                </button>
              ))}
            </div>
          </div>

          <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6">
            <h2 className="font-semibold text-gray-900 mb-4">Recent Activity</h2>
            <div className="space-y-4">
              {[
                { action: 'New user registered', time: '2 min ago' },
                { action: 'Booking confirmed #1245', time: '15 min ago' },
                { action: 'New property added', time: '1 hour ago' },
                { action: 'Payment received ₹12,500', time: '3 hours ago' },
              ].map((a, i) => (
                <div key={i} className="flex items-start gap-3">
                  <div className="w-2 h-2 rounded-full bg-primary-500 mt-2" />
                  <div>
                    <p className="text-sm text-gray-700">{a.action}</p>
                    <p className="text-xs text-gray-400">{a.time}</p>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
