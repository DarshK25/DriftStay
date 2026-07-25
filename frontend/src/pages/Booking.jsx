import React from 'react';
import { useNavigate } from 'react-router-dom';
import { useSearchParams } from 'react-router-dom';
import { ChevronLeft, Check } from 'lucide-react';
import { formatPrice, formatDate, formatDateRange, getNights } from '../lib/utils';

const FEATURED_PROPERTY = {
  id: 1,
  name: 'Sea View Villa',
  city: 'Goa',
  starCategory: 5,
  startingPrice: 12000,
  image: 'https://images.unsplash.com/photo-1564013799919-ab600027ffc6?w=800',
};

export default function Booking() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const checkIn = searchParams.get('checkIn') || '';
  const checkOut = searchParams.get('checkOut') || '';
  const guests = Number(searchParams.get('guests')) || 1;
  const nights = checkIn && checkOut ? getNights(checkIn, checkOut) : 1;
  const baseTotal = FEATURED_PROPERTY.startingPrice * nights;
  const [isBooking, setIsBooking] = React.useState(false);

  const handleConfirm = async () => {
    setIsBooking(true);
    // Simulate API call
    await new Promise(r => setTimeout(r, 1500));
    navigate(`/booking-success?ref=DRF-${Date.now().toString().slice(-6)}`);
  };

  return (
    <div className="pt-24 pb-16 min-h-screen bg-gray-50">
      <div className="max-w-4xl mx-auto px-4">
        <button onClick={() => navigate(-1)} className="flex items-center gap-1 text-sm text-gray-500 hover:text-gray-700 mb-6">
          <ChevronLeft className="w-4 h-4" /> Back
        </button>

        <div className="grid grid-cols-1 md:grid-cols-5 gap-8">
          {/* Left - Booking form */}
          <div className="md:col-span-3 space-y-6">
            <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6">
              <h2 className="text-xl font-semibold text-gray-900 mb-4">Confirm your booking</h2>
              <div className="space-y-4">
                <div className="flex gap-4">
                  <div className="flex-1">
                    <label className="block text-xs font-semibold text-gray-700 mb-1">Check-in</label>
                    <input type="date" defaultValue={checkIn} className="w-full px-3 py-2.5 border border-gray-300 rounded-xl text-sm outline-none" />
                  </div>
                  <div className="flex-1">
                    <label className="block text-xs font-semibold text-gray-700 mb-1">Check-out</label>
                    <input type="date" defaultValue={checkOut} className="w-full px-3 py-2.5 border border-gray-300 rounded-xl text-sm outline-none" />
                  </div>
                </div>
                <div>
                  <label className="block text-xs font-semibold text-gray-700 mb-1">Guests</label>
                  <select defaultValue={guests} className="w-full px-3 py-2.5 border border-gray-300 rounded-xl text-sm outline-none">
                    {[1,2,3,4,5,6,7,8].map(n => <option key={n}>{n} {n === 1 ? 'Guest' : 'Guests'}</option>)}
                  </select>
                </div>
                <div>
                  <label className="block text-xs font-semibold text-gray-700 mb-1">Special requests (optional)</label>
                  <textarea rows={3} className="w-full px-3 py-2.5 border border-gray-300 rounded-xl text-sm outline-none resize-none" placeholder="Any special requests?" />
                </div>
              </div>
            </div>

            <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6">
              <h2 className="text-xl font-semibold text-gray-900 mb-4">Cancellation policy</h2>
              <p className="text-sm text-gray-500">Free cancellation up to 48 hours before check-in. After that, 50% refund up to 24 hours before check-in.</p>
            </div>
          </div>

          {/* Right - Summary */}
          <div className="md:col-span-2">
            <div className="bg-white rounded-2xl shadow-lg border border-gray-100 p-6 sticky top-24">
              <div className="flex items-center gap-3 mb-6">
                <img src={FEATURED_PROPERTY.image} alt="" className="w-16 h-16 rounded-xl object-cover" />
                <div>
                  <h3 className="font-semibold text-gray-900">{FEATURED_PROPERTY.name}</h3>
                  <p className="text-sm text-gray-500">{FEATURED_PROPERTY.city}</p>
                </div>
              </div>

              <div className="space-y-3 pb-6 border-b border-gray-100 text-sm">
                <div className="flex justify-between"><span className="text-gray-500">Dates</span><span className="font-medium">{checkIn && checkOut ? formatDateRange(checkIn, checkOut) : 'Select dates'}</span></div>
                <div className="flex justify-between"><span className="text-gray-500">Nights</span><span className="font-medium">{nights}</span></div>
                <div className="flex justify-between"><span className="text-gray-500">Guests</span><span className="font-medium">{guests}</span></div>
              </div>

              <div className="space-y-3 py-6 border-b border-gray-100 text-sm">
                <div className="flex justify-between"><span className="text-gray-500">₹{FEATURED_PROPERTY.startingPrice} × {nights} nights</span><span>{formatPrice(baseTotal)}</span></div>
                <div className="flex justify-between"><span className="text-gray-500">Cleaning fee</span><span>{formatPrice(500)}</span></div>
                <div className="flex justify-between"><span className="text-gray-500">Service fee</span><span>{formatPrice(300)}</span></div>
              </div>

              <div className="flex justify-between pt-4 font-semibold text-lg">
                <span>Total</span><span>{formatPrice(baseTotal + 500 + 300)}</span>
              </div>

              <button onClick={handleConfirm} disabled={isBooking}
                className="w-full mt-6 bg-primary-600 text-white py-3.5 rounded-xl font-semibold hover:bg-primary-700 transition-colors disabled:opacity-50">
                {isBooking ? 'Confirming...' : 'Confirm & Pay'}
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
