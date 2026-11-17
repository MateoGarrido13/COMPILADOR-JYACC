.386
.model flat, stdcall
option casemap:none
include \masm32\include\masm32rt.inc

.data
    hola DB "hola",0
    mundo DB "mundo",0
    fmt_cadena DB "%s",0

.code
start:
    ; (PROGRAMA, --, --)
    invoke crt_printf, offset fmt_cadena, offset hola
    invoke crt_printf, offset fmt_cadena, offset mundo
    invoke crt_printf, offset fmt_cadena, offset hola
    ; (FIN_PROGRAMA, --, --)
    invoke ExitProcess, 0
end start
