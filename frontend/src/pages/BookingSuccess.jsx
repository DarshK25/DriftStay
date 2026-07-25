import React from 'react';
import { useSearchParams, Link } from 'react-router-dom';
import { CheckCircle, Calendar, MapPin } from 'lucide-react';

export default function BookingSuccess() {
  const [params] = useSearchParams();
  const ref = params.get('ref') || 'N/A';

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50 px-4">
      <div className="bg-white rounded-3xl shadow-lg border border-gray-100 p-8 md:p-12 max-w-lg w-full text-center">
        <div className="w-20 h-20 rounded-full bg-green-100 flex items-center justify-center mx-auto mb-6">
          <CheckCircle className="w-10 h-10 text-green-600" />
        </div>
        <h1 className="text-3xl font-display font-bold text-gray-900 mb-2">Booking Confirmed!</h1>
        <p className="text-gray-500 mb-6">Your booking has been successfully confirmed.</p>

        <div className="bg-gray-50 rounded-2xl p-6 space-y-4 mb-8">
          <div className="flex items-center justify-between text-sm">
            <span className="text-gray-500">Reference</span>
            <span className="font-mono font-semibold text-primary-600">{ref}</span>
          </div>
          <div className="flex items-center gap-3">
            <Calendar className="w-5 h-5 text-gray-400" />
            <div className="text-left">
              <p className="text-sm font-medium">Check-in: Apr 15, 2026</p>
              <p className="text-sm font-medium">Check-out: Apr 18, 2026</p>
            </div>
          </div>
          <div className="flex items-center gap-3">
            <MapPin className="w-5 h-5 text-gray-400" />
            <div className="text-left">
              <p className="text-sm font-medium">Sea View Villa</p>
              <p className="text-sm text-gray-500">Goa, India</p>
            </div>
          </div>
        </div>

        <div className="flex flex-col sm:flex-row gap-3">
          <Link to="/trips" className="flex-1 bg-primary-600 text-white py-3 rounded-xl font-semibold hover:bg-primary-700 transition-colors">
            View My Trips
          </Link>
          <Link to="/" className="flex-1 border border-gray-300 text-gray-700 py-3 rounded-xl font-semibold hover:bg-gray-50 transition-colors">
            Go Home
          </Link>
        </div>
      </div>
    </div>
  );
}
