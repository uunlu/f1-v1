package com.f1.seasonchampions.service.query;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.f1.seasonchampions.dto.SeasonChampionListItem;
import com.f1.seasonchampions.dto.SeasonChampionListItemImpl;
import com.f1.seasonchampions.repository.SeasonChampionRepository;
import com.f1.seasonchampions.service.query.seasonchampion.SeasonChampionQueryService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SeasonChampionQueryServiceTest {

  @Mock private SeasonChampionRepository seasonChampionRepository;

  @InjectMocks private SeasonChampionQueryService seasonChampionQueryService;

  @Test
  void getAllSeasons_shouldReturnSeasonChampionListItems() {
    // Arrange
    SeasonChampionListItem item1 =
        new SeasonChampionListItemImpl("2020", "Lewis Hamilton", "Mercedes");
    SeasonChampionListItem item2 =
        new SeasonChampionListItemImpl("2021", "Max Verstappen", "Red Bull");
    List<SeasonChampionListItem> mockList = List.of(item1, item2);

    when(seasonChampionRepository.findAllSeasonChampionListItems()).thenReturn(mockList);

    // Act
    List<SeasonChampionListItem> result = seasonChampionQueryService.getAllSeasons();

    // Assert
    assertEquals(2, result.size());
    assertEquals("2020", result.get(0).getSeason());
    verify(seasonChampionRepository).findAllSeasonChampionListItems();
  }
}
