import { useState, useRef, useCallback } from "react";
import {
  View,
  Text,
  FlatList,
  TextInput,
  Pressable,
  KeyboardAvoidingView,
  Platform,
} from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import { MaterialIcons } from "@expo/vector-icons";
import { colors } from "../theme/tokens";

interface ChatMessage {
  id: string;
  text: string;
  sender: "user" | "bot";
  timestamp: Date;
}

const WELCOME_MESSAGE: ChatMessage = {
  id: "welcome",
  text: "Namaste! Main MaalSaathi hun. Support abhi jaldi aa raha hai — tab tak aap apne trip aur kharche Trip tab se manage kar sakte ho.",
  sender: "bot",
  timestamp: new Date(),
};

const COMING_SOON_REPLY: ChatMessage = {
  id: "coming_soon",
  text: "Abhi live support available nahi hai. Jaldi aa raha hai! Tab tak Trip tab se trip log karo, kharcha daalo, aur Hisaab tab se apna P&L dekho.",
  sender: "bot",
  timestamp: new Date(),
};

function MessageBubble({ message }: { message: ChatMessage }) {
  const isBot = message.sender === "bot";

  return (
    <View className={`mb-3 max-w-[85%] ${isBot ? "self-start" : "self-end"}`}>
      <View
        className={`rounded-card px-4 py-3 ${
          isBot ? "bg-surface-elevated rounded-tl-none" : "bg-surface rounded-tr-none"
        }`}
      >
        <Text className="text-body text-text-primary">{message.text}</Text>
      </View>
      <Text
        className={`mt-1 text-sm text-text-secondary ${isBot ? "" : "text-right"}`}
      >
        {message.timestamp.toLocaleTimeString("hi-IN", {
          hour: "2-digit",
          minute: "2-digit",
        })}
      </Text>
    </View>
  );
}

export function MadadScreen() {
  const [messages, setMessages] = useState<ChatMessage[]>([WELCOME_MESSAGE]);
  const [input, setInput] = useState("");
  const listRef = useRef<FlatList<ChatMessage>>(null);

  const handleSend = useCallback(() => {
    const trimmed = input.trim();
    if (!trimmed) return;

    const userMsg: ChatMessage = {
      id: `user_${Date.now()}`,
      text: trimmed,
      sender: "user",
      timestamp: new Date(),
    };

    const botReply: ChatMessage = {
      ...COMING_SOON_REPLY,
      id: `bot_${Date.now()}`,
      timestamp: new Date(),
    };

    setMessages((prev) => [...prev, userMsg, botReply]);
    setInput("");

    setTimeout(() => {
      listRef.current?.scrollToEnd({ animated: true });
    }, 100);
  }, [input]);

  return (
    <SafeAreaView className="flex-1 bg-dark" edges={["top"]}>
      <View className="border-b border-border px-5 pb-3 pt-4">
        <Text className="text-2xl font-bold text-text-primary">Madad</Text>
        <View className="mt-1 flex-row items-center gap-1.5">
          <View className="h-2 w-2 rounded-full bg-text-secondary" />
          <Text className="text-sm text-text-secondary">Jaldi aa raha hai</Text>
        </View>
      </View>

      <KeyboardAvoidingView
        className="flex-1"
        behavior={Platform.OS === "ios" ? "padding" : "height"}
        keyboardVerticalOffset={Platform.OS === "ios" ? 90 : 0}
      >
        <FlatList
          ref={listRef}
          data={messages}
          keyExtractor={(item) => item.id}
          renderItem={({ item }) => <MessageBubble message={item} />}
          contentContainerClassName="px-5 pt-4 pb-4"
          showsVerticalScrollIndicator={false}
          onContentSizeChange={() =>
            listRef.current?.scrollToEnd({ animated: false })
          }
        />

        <View className="flex-row items-end gap-2 border-t border-border bg-surface px-4 pb-5 pt-3">
          <TextInput
            className="min-h-touch max-h-24 flex-1 rounded-button border border-border bg-surface-elevated px-4 text-body text-text-primary"
            placeholder="Message likho..."
            placeholderTextColor={colors.textSecondary}
            value={input}
            onChangeText={setInput}
            multiline
            returnKeyType="send"
            onSubmitEditing={handleSend}
            blurOnSubmit
            selectionColor={colors.primary}
          />
          <Pressable
            className={`h-12 w-12 items-center justify-center rounded-full ${
              input.trim() ? "bg-primary active:opacity-80" : "bg-surface-elevated"
            }`}
            onPress={handleSend}
            disabled={!input.trim()}
            accessibilityLabel="Send message"
          >
            <MaterialIcons
              name="send"
              size={20}
              color={input.trim() ? colors.dark : colors.textSecondary}
            />
          </Pressable>
        </View>
      </KeyboardAvoidingView>
    </SafeAreaView>
  );
}
