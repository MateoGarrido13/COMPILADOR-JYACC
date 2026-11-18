.386
.model flat, stdcall
option casemap:none
include \masm32\include\masm32rt.inc

.data
    u_main_b1 DD ?
    v_main_b2 DQ ?
    w_main_hija DD ?
    s1_b1_u_main DD ?
    s1_b2_v_main DQ ?
    s1_w_main DD ?
    s2_b1_u_main DD ?
    s2_b2_v_main DQ ?
    s2_w_main DD ?
    c_4_ul_ul DD 4
    c_1_25 DQ 1.25
    c_9_ul_ul DD 9
    fmt_ulongint DB "%u",0
    fmt_doublef DB "%f",0

.code
start:
    mov eax, c_4_ul_ul
    mov s1_b1_u_main, eax
    fld c_1_25
    fstp s1_b2_v_main
    mov eax, c_9_ul_ul
    mov s1_w_main, eax
    mov eax, s1_b1_u_main
    mov s2_b1_u_main, eax
    mov eax, dword ptr [s1_b2_v_main+4]
    mov dword ptr [s2_b2_v_main+4], eax
    mov eax, dword ptr [s1_b2_v_main]
    mov dword ptr [s2_b2_v_main], eax
    mov eax, s1_w_main
    mov s2_w_main, eax
    invoke crt_printf, offset fmt_ulongint, s2_b1_u_main
    invoke crt_printf, offset fmt_doublef, s2_b2_v_main
    invoke crt_printf, offset fmt_ulongint, s2_w_main
    invoke ExitProcess, 0
end start
