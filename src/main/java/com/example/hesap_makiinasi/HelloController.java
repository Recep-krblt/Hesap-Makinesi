package com.example.hesap_makiinasi;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.control.TextFormatter;
import javafx.scene.input.KeyEvent;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import net.objecthunter.exp4j.operator.Operator;
import net.objecthunter.exp4j.function.Function;

import java.util.function.UnaryOperator;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class HelloController {

    @FXML private TextField display; // Sonuçların ve işlemlerin gösterildiği ana ekran
    @FXML private TextArea operationLabel; // Üstteki küçük geçmiş işlem bilgi ekranı (History)

    private boolean justCalculated = false; // "Az önce hesaplama yapıldı mı?" kontrolü
    private double lastAns = 0; // Son hesaplanan sonucu (ANS) hafızada tutar

    // --- ÖZEL OPERATÖR TANIMLAMASI ---
    // exp4j kütüphanesi için Faktöriyel (!) Operatörü
    private final Operator factorial = new Operator("!", 1, true, Operator.PRECEDENCE_POWER + 1) {
        @Override
        public double apply(double... args) {
            double arg = args[0];
            // Negatif sayılar için hata fırlat
            if (arg < 0 || arg != Math.floor(arg))
                throw new IllegalArgumentException("Faktöriyel sadece negatif olmayan tam sayılar içindir.");

            if (arg > 170) return Double.POSITIVE_INFINITY;

            double result = 1;
            for (int i = 2; i <= arg; i++) result *= i;
            return result;
        }
    };

    @FXML
    private void initialize() {
        // --- KLAVYE İYİLEŞTİRMESİ (TextFormatter) ---
        UnaryOperator<TextFormatter.Change> filter = change -> {
            String text = change.getText();
            if (text.equals("*")) {
                change.setText("×");
            } else if (text.equals("/")) {
                change.setText("÷");
            }
            return change;
        };
        display.setTextFormatter(new TextFormatter<>(filter));

        display.setOnKeyTyped(this::handleKeyTyped);
        display.requestFocus();
    }

    // --- KLAVYE KONTROLÜ ---
    public void handleKeyTyped(KeyEvent event) {
        String character = event.getCharacter();

        if (character.equals("=") || character.equals("\r")) {
            event.consume();
            calculate();
            return;
        }

        // Hata mesajı varsa temizle
        if (display.getText().equals("Hata")) {
            display.setText("");
        }

        // FİLTRE: İzin verilen karakterler
        if (!character.matches("[0-9+\\-*/().^!√%×÷a-zπe| ]")) {
            event.consume();
            return;
        }

        if (justCalculated) {
            justCalculated = false;
            if (Character.isDigit(character.charAt(0))) {
                display.setText("");
            }
        }
    }

    // --- EKRANA YAZI EKLEME ---
    private void insertText(String text) {
        // Hata mesajı varsa temizle
        if (display.getText().equals("Hata")) {
            display.setText("");
        }

        if (justCalculated) {
            // Eğer işlem sonucu üzerine işlem yapılmıyorsa ekranı temizle
            if ("0123456789.√(".contains(text) || text.startsWith("sqrt") ||
                    text.startsWith("sin") || text.startsWith("cos") || text.startsWith("tan") ||
                    text.startsWith("log") || text.startsWith("ln") || text.startsWith("abs") ||
                    text.startsWith("cot") || text.startsWith("sec") || text.startsWith("csc") ||
                    text.startsWith("root") || text.equals("π") || text.equals("e")) {
                display.setText("");
            }
            justCalculated = false;
        }

        if (!display.isFocused()) {
            display.positionCaret(display.getText().length());
        }

        int caretPos = display.getCaretPosition();
        String currentText = display.getText();

        String newText = currentText.substring(0, caretPos) + text + currentText.substring(caretPos);

        display.setText(newText);
        display.positionCaret(caretPos + text.length());
    }

    // --- YENİ: AKILLI PARANTEZ YÖNTEMİ ---
    // Negatif sayıların faktöriyeli alınırken (-5)! şeklinde sarmalar
    private void insertFactorialWithSmartParenthesis() {
        String text = display.getText();
        // Sondan geriye doğru sayıyı tara
        int i = text.length() - 1;
        while (i >= 0 && (Character.isDigit(text.charAt(i)) || text.charAt(i) == '.')) {
            i--;
        }

        // Eğer sayıdan hemen önce '-' varsa
        if (i >= 0 && text.charAt(i) == '-') {
            // Bu '-' işaretinin öncesine bak: Başlangıç mı yoksa operatör mü?
            // Eğer öncesinde rakam yoksa, bu bir negatiflik işaretidir (çıkarma değildir)
            boolean isNegativeSign = (i == 0) || "+-×÷^(".indexOf(text.charAt(i - 1)) != -1;

            if (isNegativeSign) {
                // Negatif sayıyı bulduk! "-5" -> "(-5)!"
                String before = text.substring(0, i);
                String number = text.substring(i);

                display.setText(before + "(" + number + ")!");
                display.positionCaret(display.getText().length());
                justCalculated = false;
                return;
            }
        }
        // Negatif değilse normal ekle
        insertText("!");
    }

    @FXML
    private void onNumberClick(javafx.event.ActionEvent event) {
        String val = ((Button) event.getSource()).getText();
        insertText(val);
    }

    @FXML
    private void onOperatorClick(javafx.event.ActionEvent event) {
        String val = ((Button) event.getSource()).getText();

        switch (val) {
            case "xⁿ" -> insertText("^");
            case "√" -> insertText("√(");
            case "x²" -> insertText("^2");
            case "x³" -> insertText("^3");

            // DÜZELTME: Faktöriyel (!) için akıllı parantez kontrolü
            case "!" -> insertFactorialWithSmartParenthesis();

            case "×" -> insertText("×");
            case "÷" -> insertText("÷");
            case "Mod" -> insertText(" mod ");
            case "%" -> insertText("%");

            // HATA DÜZELTİLDİ: case "\|x\|" -> case "|x|"
            case "|x|" -> {
                String current = display.getText();
                String selection = display.getSelectedText();
                if (!selection.isEmpty()) {
                    display.replaceSelection("abs(" + selection + ")");
                    justCalculated = false;
                } else if (!current.isEmpty() && !"+-×÷^(".contains(current.substring(current.length() - 1))) {
                    display.setText("abs(" + current + ")");
                    display.positionCaret(display.getText().length());
                    justCalculated = false;
                } else {
                    insertText("abs(");
                }
            }
            case "ⁿ√x" -> insertText("^(1/");
            case "sin" -> insertText("sin(");
            case "cos" -> insertText("cos(");
            case "tan" -> insertText("tan(");
            case "cot" -> insertText("cot(");
            case "sec" -> insertText("sec(");
            case "csc" -> insertText("csc(");
            case "log" -> insertText("log(");
            case "ln" -> insertText("ln(");
            case "π" -> insertText("π");
            case "e" -> insertText("e");
            default -> insertText(val);
        }
    }

    @FXML
    private void onAnsClick() {
        if (lastAns == (long) lastAns) {
            insertText(String.format("%d", (long)lastAns));
        } else {
            insertText(String.valueOf(lastAns));
        }
    }

    @FXML
    private void onDelClick() {
        if (!display.isFocused()) {
            display.positionCaret(display.getText().length());
        }

        String text = display.getText();
        int caret = display.getCaretPosition();

        if (!text.isEmpty() && caret > 0) {
            String before = text.substring(0, caret - 1);
            String after = text.substring(caret);
            display.setText(before + after);
            display.positionCaret(caret - 1);
        }
    }

    @FXML
    private void onClearClick() {
        display.setText("");
        justCalculated = false;
        display.requestFocus();
    }

    @FXML
    private void onEqualClick() {
        calculate();
    }

    // --- HESAPLAMA MOTORU ---
    private void calculate() {
        String screenText = display.getText();

        if (screenText.isEmpty()) return;

        // Otomatik Parantez Kapatma
        int openParens = 0;
        int closeParens = 0;
        for (char c : screenText.toCharArray()) {
            if (c == '(') openParens++;
            if (c == ')') closeParens++;
        }
        StringBuilder fixedText = new StringBuilder(screenText);
        while (openParens > closeParens) {
            fixedText.append(")");
            closeParens++;
        }
        String textToProcess = fixedText.toString();

        try {
            // Yüzde ve Mod Dönüşümleri
            textToProcess = textToProcess.replaceAll("(?<![0-9.])%([0-9]+(\\.[0-9]+)?)", "($1/100)");
            textToProcess = textToProcess.replace("%", "/100");
            textToProcess = textToProcess.replace(" mod ", "%");

            String mathExpression = textToProcess
                    .replace("×", "*")
                    .replace("x", "*")
                    .replace("÷", "/")
                    .replace("√", "sqrt")
                    .replace("π", "pi")
                    .replace("log", "log10")
                    .replace("ln", "log");

            // Fonksiyonlar ve Hata Kontrolleri (Tanımsız durumlar için)
            Function sin = new Function("sin", 1) {
                @Override public double apply(double... args) { return Math.sin(Math.toRadians(args[0])); }
            };
            Function cos = new Function("cos", 1) {
                @Override public double apply(double... args) { return Math.cos(Math.toRadians(args[0])); }
            };
            Function tan = new Function("tan", 1) {
                @Override public double apply(double... args) {
                    if (Math.abs(args[0] % 180) == 90) throw new ArithmeticException("Tanımsız");
                    return Math.tan(Math.toRadians(args[0]));
                }
            };
            Function cot = new Function("cot", 1) {
                @Override public double apply(double... args) {
                    if (Math.abs(args[0] % 180) == 0) throw new ArithmeticException("Tanımsız");
                    return 1.0 / Math.tan(Math.toRadians(args[0]));
                }
            };
            Function sec = new Function("sec", 1) {
                @Override public double apply(double... args) {
                    if (Math.abs(args[0] % 180) == 90) throw new ArithmeticException("Tanımsız");
                    return 1.0 / Math.cos(Math.toRadians(args[0]));
                }
            };
            Function csc = new Function("csc", 1) {
                @Override public double apply(double... args) {
                    if (Math.abs(args[0] % 180) == 0) throw new ArithmeticException("Tanımsız");
                    return 1.0 / Math.sin(Math.toRadians(args[0]));
                }
            };

            Expression exp = new ExpressionBuilder(mathExpression)
                    .operator(factorial)
                    .function(sin).function(cos).function(tan)
                    .function(cot).function(sec).function(csc)
                    .build();

            double result = exp.evaluate();
            lastAns = result;

            operationLabel.appendText(fixedText.toString() + " = " + formatResult(result) + "\n");
            operationLabel.positionCaret(operationLabel.getText().length());

            display.setText(formatResult(result));
            display.positionCaret(display.getText().length());

            display.requestFocus();
            justCalculated = true;

        } catch (Exception e) {
            display.setText("ERORR");
        }
    }

    private String formatResult(double val) {
        if (Double.isInfinite(val) || Double.isNaN(val)) return String.valueOf(val);

        // Hassasiyet Hatalarını Temizleme (epsilon kontrolü)
        if (Math.abs(val) < 1e-12) val = 0.0;
        if (Math.abs(val - Math.round(val)) < 1e-12) {
            return String.format("%d", Math.round(val));
        }

        BigDecimal bd = new BigDecimal(Double.toString(val));
        bd = bd.setScale(10, RoundingMode.HALF_UP);
        double roundedVal = bd.doubleValue();

        if (roundedVal == (long) roundedVal) {
            return String.format("%d", (long) roundedVal);
        }
        return String.valueOf(roundedVal);
    }
}