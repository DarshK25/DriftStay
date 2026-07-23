package com.driftstay.room.mapper;

import com.driftstay.room.dto.response.RoomDetailResponse;
import com.driftstay.room.dto.response.RoomSummaryResponse;
import com.driftstay.room.dto.request.RoomCreateRequest;
import com.driftstay.room.dto.request.RoomUpdateRequest;
import com.driftstay.room.entity.Room;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RoomMapper {

    @Mapping(target = "thumbnailUrl", ignore = true)
    RoomSummaryResponse toSummary(Room room);

    @Mapping(target = "imageUrls", ignore = true)
    RoomDetailResponse toDetail(Room room);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "publicId", ignore = true)
    @Mapping(target = "property", ignore = true)
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "availabilities", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Room toEntity(RoomCreateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "publicId", ignore = true)
    @Mapping(target = "property", ignore = true)
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "availabilities", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(RoomUpdateRequest request, @MappingTarget Room room);

    List<RoomSummaryResponse> toSummaryList(List<Room> rooms);
}
