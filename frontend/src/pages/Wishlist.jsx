import React from 'react';
import { Heart } from 'lucide-react';
import PropertyCard from '../components/PropertyCard';

const SAMPLE_WISHLIST = [
  { name: 'Coffee Plantation Stay', city: 'Coorg', state: 'Karnataka', averageRating: 4.7, slug: 'coffee-plantation', shortDescription: 'Heritage bungalow amidst coffee estates', starCategory: 4, startingPrice: 6500 },
  { name: 'Lake View Cottage', city: 'Udaipur', state: 'Rajasthan', averageRating: 4.5, slug: 'lake-view', shortDescription: 'Romantic cottage overlooking Lake Pichola', starCategory: 4, startingPrice: 9500 },
];

export default function Wishlist() {
  return (
    <div className="pt-24 pb-16">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <h1 className="text-3xl font-display font-bold text-gray-900 mb-2">My Wishlist</h1>
        <p className="text-gray-500 mb-8">Properties you've saved</p>

        {SAMPLE_WISHLIST.length === 0 ? (
          <div className="text-center py-20 bg-gray-50 rounded-2xl">
            <Heart className="w-12 h-12 text-gray-300 mx-auto mb-4" />
            <p className="text-gray-500 text-lg">Your wishlist is empty</p>
            <p className="text-gray-400 text-sm mt-1">Save properties you love by tapping the heart icon</p>
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            {SAMPLE_WISHLIST.map(p => <PropertyCard key={p.slug} property={p} />)}
          </div>
        )}
      </div>
    </div>
  );
}
