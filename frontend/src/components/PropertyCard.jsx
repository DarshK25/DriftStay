import React from 'react';
import { Link } from 'react-router-dom';
import { Star, MapPin, Heart } from 'lucide-react';
import { formatPrice, cn } from '../lib/utils';

export default function PropertyCard({ property, featured = false }) {
  const [isFav, setIsFav] = React.useState(false);

  return (
    <div className={cn('group cursor-pointer', featured && 'col-span-2 row-span-2')}>
      <Link to={`/properties/${property.slug || property.publicId}`}>
        {/* Image */}
        <div className="relative overflow-hidden rounded-2xl aspect-[4/3] mb-3">
          <img
            src={property.thumbnailUrl || 'https://images.unsplash.com/photo-1564013799919-ab600027ffc6?w=800'}
            alt={property.name}
            className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500"
          />
          <button
            onClick={(e) => { e.preventDefault(); setIsFav(!isFav); }}
            className="absolute top-3 right-3 p-2 rounded-full bg-white/80 hover:bg-white transition-colors"
          >
            <Heart className={cn('w-5 h-5', isFav ? 'fill-red-500 text-red-500' : 'text-gray-600')} />
          </button>
          {property.starCategory && (
            <span className="absolute top-3 left-3 px-2.5 py-1 rounded-full bg-white/90 text-xs font-semibold">
              {property.starCategory}-Star
            </span>
          )}
        </div>

        {/* Info */}
        <div className="space-y-1">
          <div className="flex items-center justify-between">
            <h3 className="font-semibold text-gray-900 group-hover:text-primary-600 transition-colors line-clamp-1">
              {property.name}
            </h3>
            <div className="flex items-center gap-1 text-sm">
              <Star className="w-4 h-4 fill-yellow-400 text-yellow-400" />
              <span className="font-medium">{property.averageRating || 'New'}</span>
            </div>
          </div>
          <p className="text-sm text-gray-500 flex items-center gap-1">
            <MapPin className="w-3.5 h-3.5" /> {property.city}, {property.state}
          </p>
          <p className="text-sm text-gray-400 line-clamp-1">{property.shortDescription}</p>
          <div className="pt-1">
            <span className="font-semibold text-gray-900">{formatPrice(property.startingPrice || 5000)}</span>
            <span className="text-gray-500 text-sm"> / night</span>
          </div>
        </div>
      </Link>
    </div>
  );
}
