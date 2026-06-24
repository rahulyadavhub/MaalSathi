import { forwardRef } from "react";
import { View, Text, TextInput, type TextInputProps } from "react-native";
import { colors } from "../theme/tokens";

interface FormFieldProps extends TextInputProps {
  label: string;
  error?: string | null;
  hint?: string;
}

export const FormField = forwardRef<TextInput, FormFieldProps>(
  function FormField({ label, error, hint, ...inputProps }, ref) {
    const hasError = !!error;

    return (
      <View>
        <Text className="mb-2 text-body font-medium text-text-secondary">
          {label}
        </Text>
        <TextInput
          ref={ref}
          className={`min-h-cta rounded-button border px-4 text-lg text-text-primary ${
            hasError
              ? "border-loss bg-loss/5"
              : "border-border bg-surface-elevated focus:border-primary"
          }`}
          placeholderTextColor={colors.textSecondary}
          selectionColor={colors.primary}
          cursorColor={colors.primary}
          {...inputProps}
        />
        {hasError && (
          <Text className="mt-1.5 text-sm text-loss">{error}</Text>
        )}
        {!hasError && hint && (
          <Text className="mt-1.5 text-sm text-text-secondary">{hint}</Text>
        )}
      </View>
    );
  },
);
