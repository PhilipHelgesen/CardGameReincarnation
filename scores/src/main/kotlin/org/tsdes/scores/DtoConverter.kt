package org.tsdes.scores

import org.tsdes.scores.db.UserStats
import org.tsdes.scores.dto.UserStatsDto


object DtoConverter {
     fun transform(stats: UserStats): UserStatsDto =  stats.run {
         UserStatsDto(userId, victories, defeats, draws, score)
     }
     fun transform(scores: Iterable<UserStats>):
             List<UserStatsDto> = scores.map { transform(it) }
}