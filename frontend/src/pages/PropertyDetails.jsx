import React from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import { Star, MapPin, Wifi, Car, Waves, Utensils, Wind, Dumbbell, Check, ChevronLeft } from 'lucide-react';
import { formatPrice } from '../lib/utils';
import useAuthStore from '../store/authStore';
import SearchBar from '../components/SearchBar';

const AMENITIES = [
  { icon: Wifi, label: 'Free WiFi' },
  { icon: Car, label: 'Free Parking' },
  { icon: Waves, label: 'Swimming Pool' },
  { icon: Utensils, label: 'Kitchen' },
  { icon: Wind, label: 'AC' },
  { icon: Dumbbell, label: 'Gym' },
];

const PROPERTY = {
  name: 'Sea View Villa',
  city: 'Goa',
  state: 'Goa',
  averageRating: 4.8,
  reviewCount: 124,
  starCategory: 5,
  slug: 'sea-view',
  shortDescription: 'Luxurious beachfront villa with private infinity pool',
  description: 'Experience the ultimate beachfront luxury at our Sea View Villa. This stunning property features a private infinity pool overlooking the Arabian Sea, four spacious bedrooms with en-suite bathrooms, a modern kitchen, and a beautiful garden. Perfect for families and groups looking for an unforgettable Goa vacation.',
  startingPrice: 12000,
  images: [
    'https://images.unsplash.com/photo-1564013799919-ab600027ffc6?w=1200',
    'https://images.unsplash.com/photo-1571896349842-33c89424de2d?w=1200',
    'https://images.unsplash.com/photo-1582719508461-905c673771fd?w=1200',
  ],
};

export default function PropertyDetails() {
  const { slug } = useParams();
  const { isAuthenticated } = useAuthStore();
  const navigate = useNavigate();
  const [selectedImg, setSelectedImg] = React.useState(0);
  const [checkIn, setCheckIn] = React.useState('');
  const [checkOut, setCheckOut] = React.useState('');
  const [guests, setGuests] = React.useState(1);

  const handleBook = () => {
    if (!isAuthenticated) { navigate('/login', { state: { from: { pathname: `/properties/${slug}` } } }); return; }
    navigate(`/book/1?checkIn=${checkIn}&checkOut=${checkOut}&guests=${guests}`);
  };

  return (
    <div className="pt-20 pb-16">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <Link to="/search" className="inline-flex items-center gap-1 text-sm text-gray-500 hover:text-gray-700 mb-6">
          <ChevronLeft className="w-4 h-4" /> Back to search
        </Link>

        {/* Image Gallery */}
        <div className="grid grid-cols-1 md:grid-cols-4 gap-2 rounded-2xl overflow-hidden mb-8">
          <div className="md:col-span-2 md:row-span-2">
            <img src={PROPERTY.images[selectedImg]} alt={PROPERTY.name} className="w-full h-full object-cover min-h-[400px]" />
          </div>
          {PROPERTY.images.map((img, i) => (
            <div key={i} className={`${i === selectedImg ? 'ring-2 ring-primary-500' : ''} cursor-pointer overflow-hidden ${i === 0 ? 'hidden md:block' : ''}`}>
              <img src={img} alt="" className="w-full h-full object-cover min-h-[195px]" onClick={() => setSelectedImg(i)} />
            </div>
          ))}
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-10">
          {/* Left - Details */}
          <div className="lg:col-span-2 space-y-8">
            <div>
              <div className="flex items-start justify-between">
                <div>
                  <h1 className="text-3xl font-display font-bold text-gray-900">{PROPERTY.name}</h1>
                  <p className="text-gray-500 flex items-center gap-1 mt-1">
                    <MapPin className="w-4 h-4" /> {PROPERTY.city}, {PROPERTY.state}
                  </p>
                </div>
                <div className="flex items-center gap-1 bg-green-50 px-3 py-1.5 rounded-lg">
                  <Star className="w-5 h-5 fill-green-600 text-green-600" />
                  <span className="font-semibold text-green-700">{PROPERTY.averageRating}</span>
                  <span className="text-green-600 text-sm">({PROPERTY.reviewCount} reviews)</span>
                </div>
              </div>
              <div className="flex items-center gap-2 mt-2">
                {[1,2,3,4,5].map(i => (
                  <span key={i} className={`px-2 py-0.5 text-xs font-semibold rounded ${i <= PROPERTY.starCategory ? 'bg-amber-100 text-amber-800' : 'bg-gray-100 text-gray-400'}`}>
                    {i === PROPERTY.starCategory ? `${PROPERTY.starCategory}-Star` : ''}
                  </span>
                ))}
              </div>
            </div>

            <div>
              <h2 className="text-xl font-semibold text-gray-900 mb-3">About this property</h2>
              <p className="text-gray-600 leading-relaxed">{PROPERTY.description}</p>
            </div>

            <div>
              <h2 className="text-xl font-semibold text-gray-900 mb-4">Amenities</h2>
              <div className="grid grid-cols-2 md:grid-cols-3 gap-4">
                {AMENITIES.map((a, i) => (
                  <div key={i} className="flex items-center gap-3 p-3 bg-gray-50 rounded-xl">
                    <a.icon className="w-5 h-5 text-primary-600" />
                    <span className="text-sm text-gray-700">{a.label}</span>
                  </div>
                ))}
              </div>
            </div>
          </div>

          {/* Right - Booking Card */}
          <div className="lg:col-span-1">
            <div className="sticky top-24 bg-white rounded-2xl shadow-lg border border-gray-100 p-6">
              <div className="flex items-end justify-between mb-6">
                <div>
                  <span className="text-2xl font-bold text-gray-900">{formatPrice(PROPERTY.startingPrice)}</span>
                  <span className="text-gray-500 text-sm"> / night</span>
                </div>
                <div className="flex items-center gap-1 text-sm">
                  <Star className="w-4 h-4 fill-yellow-400 text-yellow-400" />
                  <span className="font-medium">{PROPERTY.averageRating}</span>
                </div>
              </div>

              <div className="space-y-3 mb-6">
                <div>
                  <label className="block text-xs font-semibold text-gray-700 mb-1">Check-in</label>
                  <input type="date" value={checkIn} onChange={(e) => setCheckIn(e.target.value)}
                    className="w-full px-3 py-2.5 border border-gray-300 rounded-xl text-sm outline-none focus:ring-2 focus:ring-primary-500" />
                </div>
                <div>
                  <label className="block text-xs font-semibold text-gray-700 mb-1">Check-out</label>
                  <input type="date" value={checkOut} onChange={(e) => setCheckOut(e.target.value)}
                    className="w-full px-3 py-2.5 border border-gray-300 rounded-xl text-sm outline-none focus:ring-2 focus:ring-primary-500" />
                </div>
                <div>
                  <label className="block text-xs font-semibold text-gray-700 mb-1">Guests</label>
                  <select value={guests} onChange={(e) => setGuests(Number(e.target.value))}
                    className="w-full px-3 py-2.5 border border-gray-300 rounded-xl text-sm outline-none focus:ring-2 focus:ring-primary-500">
                    {[1,2,3,4,5,6,7,8].map(n => <option key={n}>{n} {n === 1 ? 'Guest' : 'Guests'}</option>)}
                  </select>
                </div>
              </div>

              <button onClick={handleBook}
                className="w-full bg-primary-600 text-white py-3.5 rounded-xl font-semibold hover:bg-primary-700 transition-colors text-lg">
                {isAuthenticated ? 'Book Now' : 'Log in to Book'}
              </button>

              <p className="text-center text-xs text-gray-400 mt-3">You won't be charged yet</p>

              <div className="border-t border-gray-100 mt-6 pt-6 space-y-3 text-sm">
                <div className="flex justify-between"><span className="text-gray-500">Base price</span><span>{formatPrice(PROPERTY.startingPrice)}</span></div>
                <div className="flex justify-between"><span className="text-gray-500">Cleaning fee</span><span>{formatPrice(500)}</span></div>
                <div className="flex justify-between"><span className="text-gray-500">Service fee</span><span>{formatPrice(300)}</span></div>
                <div className="flex justify-between border-t border-gray-100 pt-3 font-semibold">
                  <span>Total</span><span>{formatPrice(PROPERTY.startingPrice + 500 + 300)}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
