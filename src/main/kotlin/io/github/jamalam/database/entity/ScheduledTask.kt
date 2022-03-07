package io.github.jamalam.database.entity

enum class ScheduledTaskType(val validator: (data: Map<String, String>) -> Boolean) {
    PostMessageToChannel({ data ->
        data.containsKey("channel") && data.containsKey("message")
    }),
    PostEmbedToChannel({ data ->
        data.containsKey("title") && data.containsKey("description") && data.containsKey("image") &&
                data.containsKey("author") && data.containsKey("channel")
    }),
    PostUnmutedLogs({ data ->
        data.containsKey("guild") && data.containsKey("member") && data.containsKey("moderator")
    }),
    PostChannelUnlockedLogs({ data ->
        data.contains("channel") && data.containsKey("moderator") && data.containsKey("type")
    })
}

data class ScheduledTask(
    val startTime: Long,
    val duration: Long,
    val type: ScheduledTaskType,
    val data: Map<String, String>
)
