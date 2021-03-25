package org.folio.services.util.parser.lexeme.operand;

import org.folio.services.util.parser.lexeme.Lexeme;
import org.folio.services.util.parser.lexeme.Lexicon;

import java.util.List;

/**
 * The interface for the binary operands. It has a key(field.suffix), operator, and right operand (value).
 * @see Lexeme
 * @see Lexicon
 */
public interface BinaryOperand {

  String getField();

  List<String> getBindingParams();
}
