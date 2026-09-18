setenv baudrate 115200
setenv consoledev ttyS0
setenv optargs "earlycon=sbi loglevel=9 rootwait rw"
setenv root "root=/dev/mmcblk0p2"
setenv bootargs "${root} console=${consoledev},${baudrate} ${optargs}"

setenv fdt default.dtb
setenv kernel uImage.fit

setenv fdt_addr_r 0x82000000
setenv kernel_comp_addr_r 0x81400000

fatload mmc 0:1 ${fdt_addr_r} ${fdt}
fatload mmc 0:1 ${kernel_comp_addr_r} ${kernel}

bootm ${kernel_comp_addr_r} - ${fdt_addr_r}
