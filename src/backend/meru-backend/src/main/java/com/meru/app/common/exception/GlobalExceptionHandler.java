package com.meru.app.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ProblemDetail handleRecursoNoEncontrado(RecursoNoEncontradoException ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        problem.setDetail(ex.getMessage());
        return problem;
    }

    @ExceptionHandler(CupoAgotadoException.class)
    public ProblemDetail handleCupoAgotado(CupoAgotadoException ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        problem.setDetail(ex.getMessage());
        return problem;
    }

    @ExceptionHandler(ReservaDuplicadaException.class)
    public ProblemDetail handleReservaDuplicada(ReservaDuplicadaException ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        problem.setDetail(ex.getMessage());
        return problem;
    }

    @ExceptionHandler(CancelacionFueraDeTiempoException.class)
    public ProblemDetail handleCancelacionFueraDeTiempo(CancelacionFueraDeTiempoException ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        problem.setDetail(ex.getMessage());
        return problem;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(IllegalArgumentException ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setDetail(ex.getMessage());
        return problem;
    }
}
