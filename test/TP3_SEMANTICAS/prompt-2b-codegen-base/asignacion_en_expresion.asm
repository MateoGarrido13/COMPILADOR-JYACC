.386
.model flat, stdcall
option casemap:none
include \masm32\include\masm32rt.inc

.data
    x_main DD ?
    y_main DD ?
    c_2_ul_ul DD 2
    @aux2 DD ?

.code
start:
    mov eax, c_2_ul_ul
    mov y_main, eax
    mov eax, @aux2
    mov x_main, eax
    invoke ExitProcess, 0
end start
