package org.folio.services.util.parser.lexeme.operand;

import org.folio.services.util.parser.lexeme.Lexicon;

import static java.lang.String.format;
import static org.folio.services.util.parser.lexeme.Lexicon.OPERATOR_EQUALS;
import static org.folio.services.util.parser.lexeme.Lexicon.OPERATOR_LEFT_ANCHORED_EQUALS;

public class ValueBinaryOperand extends BinaryOperandLexeme {

  public ValueBinaryOperand(String key, Lexicon operator, String value) {
    super(key, operator, value);
  }

  public static boolean isApplicable(String key) {
    return key.matches("^[0-9]{3}.value$");
  }

  @Override
  public String toSqlRepresentation() {
    StringBuilder stringBuilder = new StringBuilder();
    String[] keyParts = getKey().split("\\.");
    String iField = stringBuilder.append("\"").append("i").append(keyParts[0]).append("\"").append(".\"")
      .append(keyParts[1]).append("\"").toString();
    if (OPERATOR_LEFT_ANCHORED_EQUALS.equals(getOperator())) {
      return iField + " like ?";
    } else if (OPERATOR_EQUALS.equals(getOperator())) {
      return iField + " = ?";
    }
    throw new IllegalArgumentException(format("Operator [%s] is not supported for the given ControlField operand", getOperator().getSearchValue()));
  }
}
