import React from 'react';
import { useSearchParams } from 'react-router-dom';
import { SlidersHorizontal, X } from 'lucide-react';
import PropertyCard from '../components/PropertyCard';
import SearchBar from '../components/SearchBar';

const SAMPLE_RESULTS = [
  { name: 'Sea View Villa', city: 'Goa', state: 'Goa', averageRating: 4.8, slug: 'sea-view', shortDescription: 'Luxurious beachfront villa with private infinity pool', starCategory: 5, startingPrice: 12000 },
  { name: 'Mountain Retreat', city: 'Manali', state: 'Himachal Pradesh', averageRating: 4.9, slug: 'mountain-retreat', shortDescription: 'Cozy cottage with stunning Himalayan views', starCategory: 4, startingPrice: 8500 },
  { name: 'Coffee Plantation Stay', city: 'Coorg', state: 'Karnataka', averageRating: 4.7, slug: 'coffee-plantation', shortDescription: 'Heritage bungalow amidst coffee estates', starCategory: 4, startingPrice: 6500 },
  { name: 'Desert Camp', city: 'Jaisalmer', state: 'Rajasthan', averageRating: 4.6, slug: 'desert-camp', shortDescription: 'Luxury tented camp under the stars', starCategory: 5, startingPrice: 15000 },
  { name: 'Lake View Cottage', city: 'Udaipur', state: 'Rajasthan', averageRating: 4.5, slug: 'lake-view', shortDescription: 'Romantic cottage overlooking Lake Pichola', starCategory: 4, startingPrice: 9500 },
  { name: 'Tea Garden Bungalow', city: 'Munnar', state: 'Kerala', averageRating: 4.7, slug: 'tea-garden', shortDescription: 'Colonial bungalow in a tea plantation', starCategory: 4, startingPrice: 7200 },
];

export default function Search() {
  const [searchParams] = useSearchParams();
  const city = searchParams.get('city');
  const [showFilters, setShowFilters] = React.useState(false);
  const [priceRange, setPriceRange] = React.useState([0, 50000]);
  const [rating, setRating] = React.useState(0);

  const filtered = SAMPLE_RESULTS.filter(p => {
    if (city && !p.city.toLowerCase().includes(city.toLowerCase())) return false;
    if (rating > 0 && p.averageRating < rating) return false;
    if (p.startingPrice < priceRange[0] || p.startingPrice > priceRange[1]) return false;
    return true;
  });

  return (
    <div className="pt-24 pb-16">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="mb-8">
          <SearchBar compact />
        </div>

        <div className="flex items-center justify-between mb-6">
          <div>
            <h1 className="text-2xl font-display font-bold text-gray-900">
              {city ? `Stays in ${city}` : 'All Properties'}
            </h1>
            <p className="text-gray-500 text-sm mt-1">{filtered.length} properties found</p>
          </div>
          <button onClick={() => setShowFilters(!showFilters)}
            className="flex items-center gap-2 px-4 py-2 border border-gray-300 rounded-full text-sm font-medium hover:border-gray-400 transition-colors">
            <SlidersHorizontal className="w-4 h-4" /> Filters
          </button>
        </div>

        <div className="flex gap-8">
          {/* Filters sidebar */}
          {showFilters && (
            <div className="w-64 shrink-0 space-y-6">
              <div>
                <h3 className="font-semibold text-sm text-gray-900 mb-3">Price Range</h3>
                <div className="space-y-2">
                  <input type="range" min={0} max={50000} value={priceRange[1]}
                    onChange={(e) => setPriceRange([priceRange[0], Number(e.target.value)])}
                    className="w-full accent-primary-600" />
                  <div className="flex justify-between text-xs text-gray-500">
                    <span>₹{priceRange[0].toLocaleString()}</span>
                    <span>₹{priceRange[1].toLocaleString()}</span>
                  </div>
                </div>
              </div>

              <div>
                <h3 className="font-semibold text-sm text-gray-900 mb-3">Minimum Rating</h3>
                <div className="space-y-2">
              {[4, 3, 2, 1].map(r => (
                    <label key={r} className="flex items-center gap-2 cursor-pointer">
                      <input type="radio" name="rating" checked={rating === r} onChange={() => setRating(r)} className="accent-primary-600" />
                      <span className="text-sm">{r}+ Stars</span>
                    </label>
                  ))}
                  <label className="flex items-center gap-2 cursor-pointer">
                    <input type="radio" name="rating" checked={rating === 0} onChange={() => setRating(0)} className="accent-primary-600" />
                    <span className="text-sm">Any</span>
                  </label>
                </div>
              </div>

              <button onClick={() => { setPriceRange([0, 50000]); setRating(0); }}
                className="text-sm text-primary-600 hover:text-primary-700 font-medium">Clear filters</button>
            </div>
          )}

          {/* Results */}
          <div className="flex-1">
            {filtered.length === 0 ? (
              <div className="text-center py-20">
                <p className="text-gray-500 text-lg">No properties found matching your criteria.</p>
                <button onClick={() => { setPriceRange([0, 50000]); setRating(0); }}
                  className="mt-4 text-primary-600 font-medium">Clear filters</button>
              </div>
            ) : (
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                {filtered.map(p => <PropertyCard key={p.slug} property={p} />)}
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
