package io.github.parqueubajara.api.service;

import io.github.parqueubajara.api.dto.request.CompanyEstablishmentRequestDTO;
import io.github.parqueubajara.api.dto.request.GastronomyItemRequestDTO;
import io.github.parqueubajara.api.dto.request.HostPointRequestDTO;
import io.github.parqueubajara.api.dto.request.RestaurantRequestDTO;
import io.github.parqueubajara.api.dto.response.CompanyEstablishmentResponseDTO;
import io.github.parqueubajara.api.dto.response.GastronomyItemResponseDTO;
import io.github.parqueubajara.api.dto.response.HostPointResponseDTO;
import io.github.parqueubajara.api.dto.response.RestaurantResponseDTO;
import io.github.parqueubajara.api.dto.update.CompanyEstablishmentUpdateDTO;
import io.github.parqueubajara.api.dto.update.GastronomyItemUpdateDTO;
import io.github.parqueubajara.api.dto.update.HostPointUpdateDTO;
import io.github.parqueubajara.api.dto.update.RestaurantUpdateDTO;
import io.github.parqueubajara.api.exception.DuplicateRegistry;
import io.github.parqueubajara.api.exception.ResourceNotFoundException;
import io.github.parqueubajara.api.mapper.HostPointMapper;
import io.github.parqueubajara.api.mapper.RestaurantMapper;
import io.github.parqueubajara.api.model.GastronomyItem;
import io.github.parqueubajara.api.model.HostPoint;
import io.github.parqueubajara.api.model.Restaurant;
import io.github.parqueubajara.api.model.SystemUser;
import io.github.parqueubajara.api.model.enums.ApprovalStatus;
import io.github.parqueubajara.api.model.enums.EstablishmentType;
import io.github.parqueubajara.api.repository.GastronomyItemRepository;
import io.github.parqueubajara.api.repository.HostPointRepository;
import io.github.parqueubajara.api.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private final RestaurantRepository restaurantRepository;
    private final HostPointRepository hostPointRepository;
    private final GastronomyItemRepository gastronomyItemRepository;
    private final RestaurantMapper restaurantMapper;
    private final HostPointMapper hostPointMapper;

    @Transactional(readOnly = true)
    public CompanyEstablishmentResponseDTO getMyEstablishment(SystemUser user) {
        Optional<Restaurant> restaurant = restaurantRepository.findByOwnerId(user.getId());
        if (restaurant.isPresent()) {
            return toResponseDTO(restaurant.get());
        }

        Optional<HostPoint> hostPoint = hostPointRepository.findByOwnerId(user.getId());
        if (hostPoint.isPresent()) {
            return toResponseDTO(hostPoint.get());
        }

        throw new ResourceNotFoundException("Você ainda não possui um estabelecimento cadastrado");
    }

    @Transactional
    public CompanyEstablishmentResponseDTO createEstablishment(SystemUser user, CompanyEstablishmentRequestDTO dto) {
        if (restaurantRepository.findByOwnerId(user.getId()).isPresent()
                || hostPointRepository.findByOwnerId(user.getId()).isPresent()) {
            throw new DuplicateRegistry("Você já possui um estabelecimento cadastrado");
        }

        if (dto.type() == EstablishmentType.RESTAURANT) {
            if (dto.cuisineType() == null || dto.cuisineType().isBlank()) {
                throw new IllegalArgumentException("Tipo de culinária do restaurante é obrigatório");
            }

            RestaurantRequestDTO restaurantDTO = new RestaurantRequestDTO(
                    dto.name(), dto.description(), dto.address(), dto.phone(), dto.email(),
                    dto.webUrl(), dto.instagramUrl(), dto.mapsUrl(), false,
                    dto.cuisineType(), dto.openingHours(), dto.avgPrice(), dto.acceptsReservation(), dto.starRating()
            );
            Restaurant restaurant = restaurantMapper.toEntity(restaurantDTO);
            restaurant.setOwner(user);
            restaurant.setApprovalStatus(ApprovalStatus.PENDING);
            restaurantRepository.save(restaurant);
            return toResponseDTO(restaurant);
        }

        if (dto.hostType() == null) {
            throw new IllegalArgumentException("Tipo de hospedagem é obrigatório");
        }

        HostPointRequestDTO hostPointDTO = new HostPointRequestDTO(
                dto.name(), dto.description(), dto.address(), dto.phone(), dto.email(),
                dto.webUrl(), dto.instagramUrl(), dto.mapsUrl(), false,
                dto.hostType(), dto.numOfRooms(), dto.avgPrice(), dto.bookingUrl()
        );
        HostPoint hostPoint = hostPointMapper.toEntity(hostPointDTO);
        hostPoint.setOwner(user);
        hostPoint.setApprovalStatus(ApprovalStatus.PENDING);
        hostPointRepository.save(hostPoint);
        return toResponseDTO(hostPoint);
    }

    @Transactional
    public CompanyEstablishmentResponseDTO updateEstablishment(SystemUser user, CompanyEstablishmentUpdateDTO dto) {
        Optional<Restaurant> restaurantOpt = restaurantRepository.findByOwnerId(user.getId());
        if (restaurantOpt.isPresent()) {
            Restaurant restaurant = restaurantOpt.get();
            RestaurantUpdateDTO restaurantDTO = new RestaurantUpdateDTO(
                    dto.name(), dto.description(), dto.address(), dto.phone(), dto.email(),
                    dto.webUrl(), dto.instagramUrl(), dto.mapsUrl(), null,
                    dto.cuisineType(), dto.openingHours(), dto.avgPrice(), dto.acceptsReservation(), dto.starRating()
            );
            restaurantMapper.updateEntityFromDto(restaurantDTO, restaurant);
            restaurant.setApprovalStatus(ApprovalStatus.PENDING);
            restaurant.setActive(false);
            restaurantRepository.save(restaurant);
            return toResponseDTO(restaurant);
        }

        Optional<HostPoint> hostPointOpt = hostPointRepository.findByOwnerId(user.getId());
        if (hostPointOpt.isPresent()) {
            HostPoint hostPoint = hostPointOpt.get();
            HostPointUpdateDTO hostPointDTO = new HostPointUpdateDTO(
                    dto.name(), dto.description(), dto.address(), dto.phone(), dto.email(),
                    dto.webUrl(), dto.instagramUrl(), dto.mapsUrl(), null,
                    dto.hostType(), dto.numOfRooms(), dto.avgPrice(), dto.bookingUrl()
            );
            hostPointMapper.updateEntityFromDto(hostPointDTO, hostPoint);
            hostPoint.setApprovalStatus(ApprovalStatus.PENDING);
            hostPoint.setActive(false);
            hostPointRepository.save(hostPoint);
            return toResponseDTO(hostPoint);
        }

        throw new ResourceNotFoundException("Você ainda não possui um estabelecimento cadastrado");
    }

    @Transactional(readOnly = true)
    public List<GastronomyItemResponseDTO> listMenuItems(SystemUser user) {
        findOwnedRestaurant(user);
        return gastronomyItemRepository.findByRestaurantOwnerId(user.getId()).stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Transactional
    public GastronomyItemResponseDTO createMenuItem(SystemUser user, GastronomyItemRequestDTO dto) {
        Restaurant restaurant = findOwnedRestaurant(user);

        GastronomyItem item = new GastronomyItem();
        item.setName(dto.name());
        item.setDisplayOrder(dto.displayOrder());
        item.setActive(dto.active());
        item.setRestaurant(restaurant);

        gastronomyItemRepository.save(item);
        return toResponseDTO(item);
    }

    @Transactional
    public void updateMenuItem(SystemUser user, UUID itemId, GastronomyItemUpdateDTO dto) {
        GastronomyItem item = findOwnedMenuItem(user, itemId);

        if (dto.name() != null) item.setName(dto.name());
        if (dto.displayOrder() != null) item.setDisplayOrder(dto.displayOrder());
        if (dto.active() != null) item.setActive(dto.active());

        gastronomyItemRepository.save(item);
    }

    @Transactional
    public void deleteMenuItem(SystemUser user, UUID itemId) {
        GastronomyItem item = findOwnedMenuItem(user, itemId);
        gastronomyItemRepository.delete(item);
    }

    private Restaurant findOwnedRestaurant(SystemUser user) {
        return restaurantRepository.findByOwnerId(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Cardápio disponível apenas para restaurantes"));
    }

    private GastronomyItem findOwnedMenuItem(SystemUser user, UUID itemId) {
        GastronomyItem item = gastronomyItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item gastronômico de ID: " + itemId + " não encontrado"));

        if (item.getRestaurant() == null || !item.getRestaurant().getOwner().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Item gastronômico de ID: " + itemId + " não encontrado");
        }

        return item;
    }

    private CompanyEstablishmentResponseDTO toResponseDTO(Restaurant restaurant) {
        RestaurantResponseDTO base = restaurantMapper.toResponseDTO(restaurant);
        return new CompanyEstablishmentResponseDTO(
                base.id(), EstablishmentType.RESTAURANT, base.name(), base.description(), base.address(),
                base.phone(), base.email(), base.webUrl(), base.instagramUrl(), base.mapsUrl(), base.active(),
                restaurant.getApprovalStatus(),
                base.cuisineType(), base.openingHours(), base.avgPrice(), base.acceptsReservation(), base.starRating(),
                null, null, null,
                base.photos()
        );
    }

    private CompanyEstablishmentResponseDTO toResponseDTO(HostPoint hostPoint) {
        HostPointResponseDTO base = hostPointMapper.toResponseDTO(hostPoint);
        return new CompanyEstablishmentResponseDTO(
                base.id(), EstablishmentType.HOST_POINT, base.name(), base.description(), base.address(),
                base.phone(), base.email(), base.webUrl(), base.instagramUrl(), base.mapsUrl(), base.active(),
                hostPoint.getApprovalStatus(),
                null, null, base.avgPrice(), null, null,
                base.hostType(), base.numOfRooms(), base.bookingUrl(),
                base.photos()
        );
    }

    private GastronomyItemResponseDTO toResponseDTO(GastronomyItem item) {
        return new GastronomyItemResponseDTO(
                item.getId(), item.getName(), item.getImageUrl(), item.getDisplayOrder(), item.getActive()
        );
    }
}
